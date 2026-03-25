package com.aynu.notification.mq;

import com.aynu.common.constants.MqConstants;
import com.aynu.common.domain.dto.OrderNotifyMessage;
import com.aynu.common.enums.NotifyTypeEnum;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.notification.domain.po.SystemBroadcastsPO;
import com.aynu.notification.mapper.SystemBroadcastsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreateListener {

    private final SystemBroadcastsMapper systemBroadcastsMapper;

    /**
     * 统一处理订单通知消息（支持批量）
     * 即使发送端发送的是单个对象，只要配置了合适的 MessageConverter，此处用 List 接收也是安全的
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.notify.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_NOTIFY_KEY))
    public void handleOrderNotifyMessage(List<OrderNotifyMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            log.debug("收到空的订单通知消息列表，跳过处理");
            return;
        }

        log.info("收到消息处理请求，消息数量：{}", messages.size());

        for (OrderNotifyMessage message : messages) {
            try {
                processSingleMessage(message);
            } catch (Exception e) {
                log.error("处理单条订单消息异常，订单号：{}，错误原因：{}", message.getOrderNo(), e.getMessage());
            }
        }
    }

    /**
     * 抽取单条消息处理逻辑
     */
    private void processSingleMessage(OrderNotifyMessage message) {
        if (message == null || message.getUserId() == null) {
            return;
        }

        log.info("开始创建系统消息，订单号：{}, 目标用户：{}", message.getOrderNo(), message.getUserId());

        SystemBroadcastsPO po = new SystemBroadcastsPO();

        // 匹配消息类型并设置标题内容
        NotifyTypeEnum typeEnum = NotifyTypeEnum.of(message.getType());
        switch (typeEnum) {
            case PURCHASE_MESSAGE:
                po.setTitle("购买消息");
                po.setContent("您有新的购买消息，请及时处理，订单 id：" + message.getOrderNo());
                break;
            case REVIEW_MESSAGE:
                po.setTitle("审核消息");
                po.setContent("您有新的审核消息，请及时处理，订单 id：" + message.getOrderNo());
                break;
            default:
                log.warn("未知的消息类型：{}, 订单号：{}", message.getType(), message.getOrderNo());
                throw new BadRequestException("Invalid message type: " + message.getType());
        }

        po.setCategory(4);
        po.setTargetType(2);
        po.setTargetUserId(message.getUserId());
        po.setIsActive(true);
        long now = System.currentTimeMillis();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        systemBroadcastsMapper.insert(po);
        log.info("系统消息创建成功，用户 ID: {}", message.getUserId());
    }
}
