package com.aynu.notification.mapper;

import com.aynu.notification.domain.po.Notifications;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 通知消息表，存储系统/业务推送的消息（逻辑外键关联用户/物品/订单表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface NotificationsMapper extends BaseMapper<Notifications> {

}
