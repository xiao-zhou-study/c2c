package com.aynu.order.task;

import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.service.BorrowOrdersService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderSyncTask {

    private final BorrowOrdersService borrowOrdersService;
    private final RedissonClient redissonClient;

    private static final long PAGE_SIZE = 50;

    // 每30秒执行一次
    @Scheduled(cron = "0/30 * * * * ?")
    public void checkUnpaidOrders() {
        RLock lock = redissonClient.getLock("sync_order_lock");
        boolean acquired = false;
        try {
            acquired = lock.tryLock(0, 3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread()
                    .interrupt();
            log.error("获取分布式锁被中断", e);
            return;
        }
        if (!acquired) {
            return;
        }

        try {
            log.info("开始执行支付补偿定时任务...");

            long startTime = ZonedDateTime.now()
                    .minusMinutes(30)
                    .toInstant()
                    .toEpochMilli();

            int currentPage = 1;
            int totalSynced = 0;

            while (true) {
                Page<BorrowOrdersPO> page = borrowOrdersService.lambdaQuery()
                        .eq(BorrowOrdersPO::getStatus, 2)
                        .ge(BorrowOrdersPO::getCreatedAt, startTime)
                        .page(new Page<>(currentPage, PAGE_SIZE));

                List<BorrowOrdersPO> orders = page.getRecords();
                if (orders.isEmpty()) {
                    break;
                }

                for (BorrowOrdersPO order : orders) {
                    borrowOrdersService.syncWithAlipay(order.getId());
                }

                totalSynced += orders.size();

                if (!page.hasNext()) {
                    break;
                }
                currentPage++;
            }

            log.info("支付补偿定时任务执行完毕，共处理 {} 笔订单", totalSynced);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
