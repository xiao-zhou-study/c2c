package com.aynu.notification.controller;

import com.aynu.notification.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 聊天相关接口
 */
@RestController
@RequestMapping("/chat")
@Api(tags = "即时聊天")
public class ChatController {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/sessions")
    @ApiOperation("获取会话列表")
    public Map<String, Object> getSessions(@ApiParam("当前用户ID") @RequestParam String userId) {
        Long uid = parseLong(userId);
        if (uid == null) {
            return Map.of("code", 400, "message", "userId 参数无效");
        }

        Set<String> sessionSet = chatWebSocketHandler.getUserSessions(uid);

        List<Map<String, Object>> sessions = sessionSet.stream()
                .map(peerIdStr -> {
                    Long peerId = Long.parseLong(peerIdStr);
                    Map<String, Object> session = new LinkedHashMap<>();
                    session.put("userId", peerId);
                    session.put("online", chatWebSocketHandler.isOnline(peerId));

                    String lastMsg = chatWebSocketHandler.getLastMessage(uid, peerId);
                    session.put("lastMessage", lastMsg != null ? parseLastMessage(lastMsg) : null);
                    session.put("unreadCount", chatWebSocketHandler.getUnreadCount(uid, peerId));
                    return session;
                })
                .sorted((a, b) -> {
                    long timeA = getTimeFromSession(a);
                    long timeB = getTimeFromSession(b);
                    return Long.compare(timeB, timeA);
                })
                .toList();

        return Map.of("code", 200, "message", "success", "data", sessions);
    }

    @GetMapping("/history")
    @ApiOperation("获取聊天记录")
    public Map<String, Object> getHistory(@ApiParam("用户A的ID") @RequestParam String userId1,
                                          @ApiParam("用户B的ID") @RequestParam String userId2) {
        Long uid1 = parseLong(userId1);
        Long uid2 = parseLong(userId2);
        if (uid1 == null || uid2 == null) {
            return Map.of("code", 400, "message", "userId 参数无效");
        }

        List<String> messages = chatWebSocketHandler.getChatHistory(uid1, uid2);

        // 拉取历史时清除未读
        chatWebSocketHandler.clearUnreadCount(uid1, uid2);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", messages);
        result.put("total", messages.size());
        return result;
    }

    @GetMapping("/online")
    @ApiOperation("查询用户是否在线")
    public Map<String, Object> checkOnline(@ApiParam("用户ID") @RequestParam String userId) {
        Long uid = parseLong(userId);
        if (uid == null) {
            return Map.of("code", 400, "message", "userId 参数无效");
        }
        return Map.of("code", 200, "message", "success", "data", chatWebSocketHandler.isOnline(uid));
    }

    /**
     * 解析 Long，兼容 "undefined" / null / 空字符串 等非法输入
     */
    private Long parseLong(String val) {
        if (val == null || val.isBlank() || "undefined".equals(val) || "null".equals(val)) {
            return null;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, String> parseLastMessage(String msg) {
        try {
            JsonNode node = objectMapper.readTree(msg);
            Map<String, String> result = new LinkedHashMap<>();
            result.put("msgId", node.path("msgId").asText(""));
            result.put("content", node.path("content").asText(""));
            result.put("from", node.path("from").asText(""));
            result.put("timestamp", node.path("timestamp").asText(""));
            return result;
        } catch (Exception e) {
            Map<String, String> result = new LinkedHashMap<>();
            result.put("content", msg);
            return result;
        }
    }

    private long getTimeFromSession(Map<String, Object> session) {
        Object lastMsg = session.get("lastMessage");
        if (lastMsg instanceof Map map) {
            Object ts = map.get("timestamp");
            if (ts != null) {
                try {
                    return Long.parseLong(ts.toString());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }
}
