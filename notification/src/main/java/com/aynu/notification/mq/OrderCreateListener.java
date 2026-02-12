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

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreateListener {
    private final SystemBroadcastsMapper systemBroadcastsMapper;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_NOTIFY_KEY))
    public void handleOrderDelayMessage(OrderNotifyMessage message) {
        log.info("收到创建消息任务，订单号：{},目标用户：{}", message.getOrderNo(), message.getUserId());
        SystemBroadcastsPO po = new SystemBroadcastsPO();

        switch (NotifyTypeEnum.of(message.getType())) {
            case BORROW_MESSAGE:
                po.setTitle("借用消息");
                po.setContent("您有新的借用消息，请及时处理，订单id：" + message.getOrderNo());
                break;
            case REVIEW_MESSAGE:
                po.setTitle("审核消息");
                po.setContent("您有新的审核消息，请及时处理，订单id：" + message.getOrderNo());
                break;
            case RETURN_MESSAGE:
                po.setTitle("归还消息");
                po.setContent("您有新的归还消息，请及时处理，订单id：" + message.getOrderNo());
                break;
            default:
                throw new BadRequestException("Invalid message type");
        }

        po.setCategory(4);
        po.setTargetType(2);
        po.setTargetUserId(message.getUserId());
        po.setIsActive(true);
        po.setCreatedAt(System.currentTimeMillis());
        po.setUpdatedAt(System.currentTimeMillis());
        systemBroadcastsMapper.insert(po);
        log.info("创建消息任务完成，订单号：{},目标用户：{}", message.getOrderNo(), message.getUserId());
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.list.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_NOTIFY_KEY))
    public void handleOrderDelayMessageList(List<OrderNotifyMessage> rejectMsgList) {
        for (OrderNotifyMessage message : rejectMsgList) {
            handleOrderDelayMessage(message);
        }
    }

}