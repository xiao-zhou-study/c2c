package com.aynu.order.mq;

import com.aynu.common.constants.MqConstants;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderDelayListener {

    private final BorrowOrdersMapper orderMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_DELAY_KEY))
    public void handleOrderDelayMessage(String orderNo) {
        log.info("收到延迟关单任务，订单号：{}", orderNo);

        BorrowOrdersPO order = orderMapper.selectOne(new LambdaQueryWrapper<BorrowOrdersPO>().eq(BorrowOrdersPO::getOrderNo,
                orderNo));

        if (order != null && order.getStatus() == 1) {
            order.setStatus(7);
            order.setCancelReason("出借人24小时内未响应，系统自动关单");
            order.setUpdatedAt(System.currentTimeMillis());
            orderMapper.updateById(order);

            String lockKey = "order:lock:" + order.getBorrowerId() + ":" + order.getItemId();
            stringRedisTemplate.delete(lockKey);

            log.info("订单 {} 超时未确认，已自动取消并释放锁", orderNo);
        } else {
            log.info("订单 {} 状态已变更，无需处理", orderNo);
        }
    }
}