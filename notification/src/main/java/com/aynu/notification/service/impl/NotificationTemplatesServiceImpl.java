package com.aynu.notification.service.impl;

import com.aynu.notification.domain.po.NotificationTemplates;
import com.aynu.notification.mapper.NotificationTemplatesMapper;
import com.aynu.notification.service.INotificationTemplatesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通知模板表，存储消息模板，用于动态生成通知内容 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Service
public class NotificationTemplatesServiceImpl extends ServiceImpl<NotificationTemplatesMapper, NotificationTemplates> implements INotificationTemplatesService {

}
