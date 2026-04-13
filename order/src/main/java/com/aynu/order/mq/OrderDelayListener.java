package com.aynu.order.mq;

import com.aynu.common.constants.MqConstants;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.aynu.order.service.BorrowOrdersService;
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
    private final BorrowOrdersService borrowOrdersService;

    /**
     * 延迟关单任务：24 小时后检查卖家是否确认订单
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.queue", durable = "true"), exchange = @Exchange(name = MqConstants.Exchange.ORDER_DELAY_EXCHANGE, type = "x-delayed-message", arguments = @Argument(name = "x-delayed-type", value = "topic")), key = MqConstants.Key.ORDER_DELAY_KEY))
    public void handleOrderDelayMessage(String orderNo) {
        log.info("收到延迟关单任务，订单号：{}", orderNo);

        BorrowOrdersPO order = orderMapper.selectOne(new LambdaQueryWrapper<BorrowOrdersPO>().eq(BorrowOrdersPO::getId,
                orderNo));

        // 状态 1 表示待确认，如果 24 小时后还是 1，则说明卖家没处理
        if (order != null && order.getStatus() == 1) {
            order.setStatus(6);
            order.setCancelReason("卖家 24 小时内未响应，系统自动关单");
            order.setUpdatedAt(System.currentTimeMillis());
            orderMapper.updateById(order);

            // 删除幂等锁，允许用户再次购买
            String lockKey = "order:lock:" + order.getBuyerId() + ":" + order.getItemId();
            stringRedisTemplate.delete(lockKey);

            log.info("订单 {} 超时未确认，已自动取消并释放锁", orderNo);
        } else {
            log.info("订单 {} 状态已变更或不存在，无需处理", orderNo);
        }
    }

    /**
     * 支付补偿任务：卖家同意订单 15 分钟后，主动查询支付宝确认支付状态
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.pay.query.queue", durable = "true"), exchange = @Exchange(name = MqConstants.Exchange.ORDER_DELAY_EXCHANGE, type = "x-delayed-message", arguments = @Argument(name = "x-delayed-type", value = "topic")), key = MqConstants.Key.ORDER_PAY_DELAY_KEY))
    public void handleOrderPayDelayMessage(String orderNo) {
        log.info("收到支付补偿任务，订单号：{}", orderNo);

        BorrowOrdersPO order = orderMapper.selectOne(new LambdaQueryWrapper<BorrowOrdersPO>().eq(BorrowOrdersPO::getId,
                orderNo));

        // 状态 2 表示待付款，说明可能未收到支付宝回调，主动查询确认
        if (order != null && order.getStatus() == 2) {
            log.info("订单 {} 仍为待付款状态，开始主动查询支付宝", orderNo);
            borrowOrdersService.syncWithAlipay(orderNo);
        } else {
            log.info("订单 {} 状态已变更为 {}，无需补偿", orderNo, order != null ? order.getStatus() : "null");
        }
    }

    /**
     * 延迟确认收货任务：超时自动将订单置为待评价
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.order.return.queue", durable = "true"), exchange = @Exchange(name = MqConstants.Exchange.ORDER_DELAY_EXCHANGE, type = "x-delayed-message", arguments = @Argument(name = "x-delayed-type", value = "topic")), key = MqConstants.Key.ORDER_RETURN_DELAY_KEY))
    public void handleOrderReturnDelayMessage(String orderNo) {
        log.info("收到延迟确认收货任务，订单号：{}", orderNo);

        LambdaQueryChainWrapper<BorrowOrdersPO> wrapper = new LambdaQueryChainWrapper<>(orderMapper);
        BorrowOrdersPO ordersPO = wrapper.eq(BorrowOrdersPO::getId, orderNo)
                .one();

        // 状态 3 表示交易中，7天后用户未确认收货，自动置为待评价
        if (ordersPO != null && ordersPO.getStatus()
                .equals(3)) {
            ordersPO.setStatus(4);
            ordersPO.setUpdatedAt(System.currentTimeMillis());
            orderMapper.updateById(ordersPO);
            log.info("订单 {} 已通过延迟任务自动置为待评价状态", orderNo);
        }
    }
}
