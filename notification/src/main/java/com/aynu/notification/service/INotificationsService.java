package com.aynu.notification.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.notification.domain.po.Notifications;
import com.aynu.notification.domain.vo.NotificationVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 通知消息表，存储系统/业务推送的消息（逻辑外键关联用户/物品/订单表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface INotificationsService extends IService<Notifications> {

    /**
     * 获取通知列表
     * @param type 通知类型
     * @param isRead 是否已读
     * @param query 分页查询条件
     * @return 通知列表
     */
    PageDTO<NotificationVO> listNotifications(String type, Boolean isRead, PageQuery query);

    /**
     * 获取未读通知数量
     * @return 未读数量
     */
    Long getUnreadCount();

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead();

    /**
     * 删除通知
     * @param notificationId 通知ID
     */
    void deleteNotification(Long notificationId);

    /**
     * 清空所有通知
     */
    void clearAllNotifications();

    /**
     * 获取最新通知
     * @param limit 数量限制
     * @return 通知列表
     */
    List<NotificationVO> getLatestNotifications(Integer limit);
}
