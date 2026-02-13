package com.aynu.order.mq;

import com.aynu.common.constants.MqConstants;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderDelayListener {

    private final BorrowOrdersMapper orderMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 延迟关单任务：24小时后检查出借人是否确认
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_DELAY_EXCHANGE,
                    type = "x-delayed-message",
                    arguments = @Argument(name = "x-delayed-type", value = "topic")),
            key = MqConstants.Key.ORDER_DELAY_KEY))
    public void handleOrderDelayMessage(String orderNo) {
        log.info("收到延迟关单任务，订单号：{}", orderNo);

        BorrowOrdersPO order = orderMapper.selectOne(new LambdaQueryWrapper<BorrowOrdersPO>().eq(BorrowOrdersPO::getOrderNo,
                orderNo));

        // 状态1表示待确认，如果24小时后还是1，则说明出借人没处理
        if (order != null && order.getStatus() == 1) {
            order.setStatus(7);
            order.setCancelReason("出借人24小时内未响应，系统自动关单");
            order.setUpdatedAt(System.currentTimeMillis());
            orderMapper.updateById(order);

            // 删除幂等锁，允许用户再次申请
            String lockKey = "order:lock:" + order.getBorrowerId() + ":" + order.getItemId();
            stringRedisTemplate.delete(lockKey);

            log.info("订单 {} 超时未确认，已自动取消并释放锁", orderNo);
        } else {
            log.info("订单 {} 状态已变更或不存在，无需处理", orderNo);
        }
    }

    /**
     * 延迟还单任务：处理归还相关的后续逻辑
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.return.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_DELAY_EXCHANGE,
                    type = "x-delayed-message",
                    arguments = @Argument(name = "x-delayed-type", value = "topic")),
            key = MqConstants.Key.ORDER_RETURN_DELAY_KEY))
    public void handleOrderReturnDelayMessage(String orderNo) {
        log.info("收到延迟还订单任务，订单号：{}", orderNo);

        LambdaQueryChainWrapper<BorrowOrdersPO> wrapper = new LambdaQueryChainWrapper<>(orderMapper);
        BorrowOrdersPO ordersPO = wrapper.eq(BorrowOrdersPO::getOrderNo, orderNo)
                .one();

        if (ordersPO != null && ordersPO.getStatus()
                .equals(4)) {
            ordersPO.setStatus(5);
            ordersPO.setUpdatedAt(System.currentTimeMillis());
            orderMapper.updateById(ordersPO);
            log.info("订单 {} 已通过延迟任务处理归还状态", orderNo);
        }

        // TODO: 退还押金给借用者，给出借者创建通知，结算金额
    }
}