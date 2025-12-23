package com.aynu.notification.service;

import com.aynu.notification.domain.po.Notifications;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 通知消息表，存储系统/业务推送的消息（逻辑外键关联用户/物品/订单表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface INotificationsService extends IService<Notifications> {

}
