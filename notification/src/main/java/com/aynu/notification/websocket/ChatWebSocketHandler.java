package com.aynu.notification.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 聊天处理器
 * 处理用户之间即时聊天消息（与订单无关，纯用户维度）
 *
 * Redis 存储:
 *   chat:messages:{smallId}:{largeId}  → List, 两个用户之间的聊天消息，按 userId 大小排序保证 key 唯一
 *   chat:sessions:{userId}             → Set, 用户的会话列表（存对方 userId）
 *   chat:unread:{userId}:{peerId}      → String, 用户A对用户B的未读消息数
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** 在线用户: userId -> WebSocketSession */
    private static final ConcurrentHashMap<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    private static final String MESSAGES_KEY_PREFIX = "chat:messages:";
    private static final String SESSIONS_KEY_PREFIX = "chat:sessions:";
    private static final String UNREAD_KEY_PREFIX = "chat:unread:";
    private static final Duration TTL = Duration.ofDays(30);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            try {
                session.close(CloseStatus.BAD_DATA);
                return;
            } catch (Exception ignored) {
            }
        }
        ONLINE_USERS.put(userId, session);
        log.info("WebSocket connected: userId={}, online={}", userId, ONLINE_USERS.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        String payload = message.getPayload();
        JsonNode json;
        try {
            json = objectMapper.readTree(payload);
        } catch (Exception e) {
            sendToSession(session, newMessage("error", null, null, null, "消息格式错误", null));
            return;
        }

        String type = json.path("type").asText();

        if ("ping".equals(type)) {
            sendToSession(session, newMessage("pong", null, null, null, null, null));
            return;
        }

        if (!"chat".equals(type)) return;

        Long toUserId = json.path("to").asLong(0);
        String content = json.path("content").asText("");
        long timestamp = json.path("timestamp").asLong(System.currentTimeMillis() / 1000);

        if (toUserId == 0 || content.isEmpty()) {
            sendToSession(session, newMessage("error", null, null, null, "参数不完整", null));
            return;
        }

        // 按 userId 大小排序，保证同一个会话的 Redis key 唯一
        long smallId = Math.min(userId, toUserId);
        long largeId = Math.max(userId, toUserId);
        String messagesKey = MESSAGES_KEY_PREFIX + smallId + ":" + largeId;

        // 生成消息唯一 ID
        String msgId = UUID.randomUUID().toString().replace("-", "");

        // 构建消息对象（from 由服务端填充，防止伪造）
        String chatMsg = newMessage("chat", msgId, userId, toUserId, content, timestamp);

        // 存储消息到 Redis
        stringRedisTemplate.opsForList().rightPush(messagesKey, chatMsg);
        stringRedisTemplate.expire(messagesKey, TTL);

        // 把对方 userId 加入会话列表
        stringRedisTemplate.opsForSet().add(SESSIONS_KEY_PREFIX + userId, String.valueOf(toUserId));
        stringRedisTemplate.opsForSet().add(SESSIONS_KEY_PREFIX + toUserId, String.valueOf(userId));
        stringRedisTemplate.expire(SESSIONS_KEY_PREFIX + userId, TTL);
        stringRedisTemplate.expire(SESSIONS_KEY_PREFIX + toUserId, TTL);

        // 发送给接收方（如果在线）
        WebSocketSession toSession = ONLINE_USERS.get(toUserId);
        if (toSession != null && toSession.isOpen()) {
            sendToSession(toSession, chatMsg);
            log.info("Chat msg delivered: from={} to={}", userId, toUserId);
        } else {
            // 对方不在线，消息入队并增加未读数
            String unreadKey = UNREAD_KEY_PREFIX + toUserId + ":" + userId;
            stringRedisTemplate.opsForValue().increment(unreadKey);
            stringRedisTemplate.expire(unreadKey, TTL);
            log.info("Chat msg queued (user offline): from={} to={}", userId, toUserId);
        }

        // 同时返回给发送方确认
        sendToSession(session, chatMsg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            log.info("WebSocket disconnected: userId={}, online={}", userId, ONLINE_USERS.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("WebSocket transport error: userId={}", userId, exception);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    /**
     * 分页获取两个用户之间的聊天记录（升序：旧消息在前，新消息在后）
     */
    public List<String> getChatHistory(Long userId1, Long userId2, int page, int size) {
        long smallId = Math.min(userId1, userId2);
        long largeId = Math.max(userId1, userId2);
        String key = MESSAGES_KEY_PREFIX + smallId + ":" + largeId;

        // Redis List 是按时间顺序 append，旧消息在前，新消息在后
        long start = (long) page * size;
        long end = start + size - 1;

        List<String> list = stringRedisTemplate.opsForList().range(key, start, end);
        return list != null ? list : Collections.emptyList();
    }

    /**
     * 获取两个用户之间的消息总数
     */
    public long getMessageCount(Long userId1, Long userId2) {
        long smallId = Math.min(userId1, userId2);
        long largeId = Math.max(userId1, userId2);
        String key = MESSAGES_KEY_PREFIX + smallId + ":" + largeId;
        Long count = stringRedisTemplate.opsForList().size(key);
        return count != null ? count : 0;
    }

    /**
     * 获取用户的会话列表（对方 userId 集合）
     */
    public Set<String> getUserSessions(Long userId) {
        String key = SESSIONS_KEY_PREFIX + userId;
        Set<String> sessions = stringRedisTemplate.opsForSet().members(key);
        return sessions != null ? sessions : Collections.emptySet();
    }

    /**
     * 获取某个会话的最后一条消息
     */
    public String getLastMessage(Long userId1, Long userId2) {
        long smallId = Math.min(userId1, userId2);
        long largeId = Math.max(userId1, userId2);
        String key = MESSAGES_KEY_PREFIX + smallId + ":" + largeId;
        List<String> list = stringRedisTemplate.opsForList().range(key, -1, -1);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * 获取用户对某个会话的未读消息数
     */
    public int getUnreadCount(Long userId, Long peerId) {
        String key = UNREAD_KEY_PREFIX + userId + ":" + peerId;
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 清除用户对某个会话的未读消息数
     */
    public void clearUnreadCount(Long userId, Long peerId) {
        String key = UNREAD_KEY_PREFIX + userId + ":" + peerId;
        stringRedisTemplate.delete(key);
    }

    private String newMessage(String type, String msgId, Long from, Long to, String content, Long timestamp) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", type);
            if (msgId != null) map.put("msgId", msgId);
            if (from != null) map.put("from", from);
            if (to != null) map.put("to", to);
            if (content != null) map.put("content", content);
            if (timestamp != null) map.put("timestamp", timestamp);
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"content\":\"服务器内部错误\"}";
        }
    }

    private void sendToSession(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) {
            log.error("Failed to send WebSocket message", e);
        }
    }
}
