package com.aynu.notification.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.notification.domain.po.Notifications;
import com.aynu.notification.domain.vo.NotificationVO;
import com.aynu.notification.mapper.NotificationsMapper;
import com.aynu.notification.service.INotificationsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知消息表，存储系统/业务推送的消息（逻辑外键关联用户/物品/订单表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper, Notifications> implements INotificationsService {

    private final UserClient userClient;

    @Override
    public PageDTO<NotificationVO> listNotifications(String type, Boolean isRead, PageQuery query) {
        Long currentUserId = UserContext.getUser();

        var wrapper = lambdaQuery()
                .eq(Notifications::getReceiverId, currentUserId)
                .eq(Notifications::getIsDeleted, false);

        if (StrUtil.isNotBlank(type)) {
            wrapper.eq(Notifications::getType, type);
        }

        if (isRead != null) {
            wrapper.eq(Notifications::getIsRead, isRead);
        }

        wrapper.orderByDesc(Notifications::getCreatedAt);

        Page<Notifications> page = wrapper.page(query.toMpPage());
        List<Notifications> records = page.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        List<NotificationVO> list = convertToVOList(records);
        return PageDTO.of(page, list);
    }

    @Override
    public Long getUnreadCount() {
        Long currentUserId = UserContext.getUser();

        return lambdaQuery()
                .eq(Notifications::getReceiverId, currentUserId)
                .eq(Notifications::getIsRead, false)
                .eq(Notifications::getIsDeleted, false)
                .count();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Long currentUserId = UserContext.getUser();

        Notifications notification = lambdaQuery()
                .eq(Notifications::getId, notificationId)
                .eq(Notifications::getReceiverId, currentUserId)
                .one();

        if (notification == null) {
            throw new BadRequestException("通知不存在");
        }

        notification.setIsRead(true);
        notification.setReadAt(System.currentTimeMillis());
        notification.setUpdatedAt(System.currentTimeMillis());
        updateById(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = UserContext.getUser();

        lambdaUpdate()
                .eq(Notifications::getReceiverId, currentUserId)
                .eq(Notifications::getIsRead, false)
                .eq(Notifications::getIsDeleted, false)
                .set(Notifications::getIsRead, true)
                .set(Notifications::getReadAt, System.currentTimeMillis())
                .set(Notifications::getUpdatedAt, System.currentTimeMillis())
                .update();
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Long currentUserId = UserContext.getUser();

        Notifications notification = lambdaQuery()
                .eq(Notifications::getId, notificationId)
                .eq(Notifications::getReceiverId, currentUserId)
                .one();

        if (notification == null) {
            throw new BadRequestException("通知不存在");
        }

        notification.setIsDeleted(true);
        notification.setUpdatedAt(System.currentTimeMillis());
        updateById(notification);
    }

    @Override
    @Transactional
    public void clearAllNotifications() {
        Long currentUserId = UserContext.getUser();

        lambdaUpdate()
                .eq(Notifications::getReceiverId, currentUserId)
                .eq(Notifications::getIsDeleted, false)
                .set(Notifications::getIsDeleted, true)
                .set(Notifications::getUpdatedAt, System.currentTimeMillis())
                .update();
    }

    @Override
    public List<NotificationVO> getLatestNotifications(Integer limit) {
        Long currentUserId = UserContext.getUser();

        List<Notifications> notifications = lambdaQuery()
                .eq(Notifications::getReceiverId, currentUserId)
                .eq(Notifications::getIsDeleted, false)
                .orderByDesc(Notifications::getCreatedAt)
                .last("LIMIT " + limit)
                .list();

        return convertToVOList(notifications);
    }

    private List<NotificationVO> convertToVOList(List<Notifications> notifications) {
        if (CollUtil.isEmpty(notifications)) {
            return Collections.emptyList();
        }

        // 收集发送人ID
        Set<Long> senderIds = notifications.stream()
                .map(Notifications::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 获取用户信息
        Map<Long, UserDTO> userMap = Map.of();
        if (CollUtil.isNotEmpty(senderIds)) {
            List<UserDTO> users = userClient.queryUserByIds(senderIds);
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));
        }

        // 转换为VO
        Map<Long, UserDTO> finalUserMap = userMap;
        return notifications.stream().map(notification -> {
            NotificationVO vo = BeanUtil.toBean(notification, NotificationVO.class);

            // 设置发送人信息
            if (notification.getSenderId() != null) {
                UserDTO sender = finalUserMap.get(notification.getSenderId());
                if (sender != null) {
                    vo.setSenderName(sender.getUsername());
                    vo.setSenderAvatar(sender.getAvatarUrl());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
