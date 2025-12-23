package com.aynu.notification.controller;

import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.notification.domain.po.Notifications;
import com.aynu.notification.domain.vo.NotificationVO;
import com.aynu.notification.service.INotificationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知消息表，存储系统/业务推送的消息（逻辑外键关联用户/物品/订单表） 前端控制器
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Api(tags = "通知管理接口")
public class NotificationsController {

    private final INotificationsService notificationsService;
    private final UserClient userClient;

    @ApiOperation("获取通知列表")
    @GetMapping
    public PageDTO<NotificationVO> listNotifications(@RequestParam(required = false) String type,
                                                    @RequestParam(required = false) Boolean isRead,
                                                    PageQuery query) {
        return notificationsService.listNotifications(type, isRead, query);
    }

    @ApiOperation("获取未读通知数量")
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount() {
        Long count = notificationsService.getUnreadCount();
        return Map.of("count", count);
    }

    @ApiOperation("标记通知为已读")
    @PostMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable("notificationId") Long notificationId) {
        notificationsService.markAsRead(notificationId);
    }

    @ApiOperation("标记所有通知为已读")
    @PostMapping("/read-all")
    public void markAllAsRead() {
        notificationsService.markAllAsRead();
    }

    @ApiOperation("删除通知")
    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable("notificationId") Long notificationId) {
        notificationsService.deleteNotification(notificationId);
    }

    @ApiOperation("清空所有通知")
    @PostMapping("/clear-all")
    public void clearAllNotifications() {
        notificationsService.clearAllNotifications();
    }

    @ApiOperation("获取最新通知")
    @GetMapping("/latest")
    public List<NotificationVO> getLatestNotifications(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        return notificationsService.getLatestNotifications(limit);
    }
}
