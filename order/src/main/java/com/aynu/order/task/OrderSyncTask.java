package com.aynu.order.task;

import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.service.BorrowOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderSyncTask {

    @Autowired
    private BorrowOrdersService borrowOrdersService;

    @Autowired
    private RedissonClient redissonClient;

    // 每5分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void checkUnpaidOrders() {
        // 1. 使用分布式锁，防止多个微服务节点同时跑任务
        RLock lock = redissonClient.getLock("sync_order_lock");
        if (!lock.tryLock()) return;

        try {
            log.info("开始执行支付补偿定时任务...");

            // 2. 查询 15 分钟前创建且未支付的订单
            long epochMilli = ZonedDateTime.now()
                    .minusMinutes(15)
                    .toInstant()
                    .toEpochMilli();
            List<BorrowOrdersPO> borrowOrdersPOS = borrowOrdersService.lambdaQuery()
                    .eq(BorrowOrdersPO::getStatus, 2)
                    .ge(BorrowOrdersPO::getCreatedAt, epochMilli)
                    .le(BorrowOrdersPO::getCreatedAt, System.currentTimeMillis())
                    .list();

            for (BorrowOrdersPO order : borrowOrdersPOS) {
                // 3. 调用支付宝主动查询接口
                // 这里的 syncWithAlipay 内部封装了 alipay.trade.query
                borrowOrdersService.syncWithAlipay(order.getOrderNo());
            }

        } finally {
            lock.unlock();
        }
    }
}
