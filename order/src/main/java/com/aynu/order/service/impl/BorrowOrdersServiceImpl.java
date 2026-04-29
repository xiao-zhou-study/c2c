package com.aynu.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.aynu.api.client.item.ItemClient;
import com.aynu.api.client.review.ReviewClient;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
import com.aynu.api.dto.review.ReviewCreateDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.api.enums.item.ItemStatus;
import com.aynu.api.enums.order.OrderStatus;
import com.aynu.common.autoconfigure.mq.RabbitMqHelper;
import com.aynu.common.domain.dto.OrderNotifyMessage;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.dto.UpdateStatsDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.order.config.AlipayProperties;
import com.aynu.order.domain.dto.*;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.domain.vo.BorrowOrdersAmountVO;
import com.aynu.order.domain.vo.BorrowOrdersPieVO;
import com.aynu.order.domain.vo.BorrowOrdersTrendVO;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.aynu.order.service.BorrowOrdersService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.aynu.common.constants.MqConstants.Exchange.*;
import static com.aynu.common.constants.MqConstants.Key.*;
import static com.aynu.common.enums.NotifyTypeEnum.PURCHASE_MESSAGE;
import static com.aynu.common.enums.NotifyTypeEnum.REVIEW_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrdersPO> implements BorrowOrdersService {

    private final ItemClient itemClient;
    private final ReviewClient reviewClient;
    private final RabbitMqHelper rabbitMqHelper;
    private final UserClient userClient;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final RedissonClient redissonClient;
    private final BorrowOrdersMapper borrowOrdersMapper;

    @Resource(name = "orderExecutor")
    private final Executor orderExecutor;

    /**
     * PO → VO 转换，自动填充物品和买卖双方用户信息
     */
    private BorrowOrdersVO toVO(BorrowOrdersPO record, ItemsVO item, UserDTO buyer, UserDTO seller) {
        BorrowOrdersVO vo = new BorrowOrdersVO();
        vo.setId(record.getId());
        vo.setItemId(record.getItemId());
        vo.setItemName(item.getTitle());
        vo.setItemImageUrl(item.getImages());
        vo.setBuyerId(record.getBuyerId());
        vo.setBuyerName(buyer.getUsername());
        vo.setBuyerAvatarUrl(buyer.getAvatarUrl());
        vo.setSellerId(record.getSellerId());
        vo.setSellerName(seller.getUsername());
        vo.setSellerAvatarUrl(seller.getAvatarUrl());
        vo.setTitle(record.getTitle());
        vo.setPrice(record.getPrice());
        vo.setStatus(record.getStatus());
        vo.setPurpose(record.getPurpose());
        vo.setConfirmTime(record.getConfirmTime());
        vo.setPayTime(record.getPayTime());
        vo.setPayTradeNo(record.getPayTradeNo());
        vo.setBorrowTime(record.getBorrowTime());
        vo.setCancelReason(record.getCancelReason());
        vo.setVersion(record.getVersion());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setUpdatedAt(record.getUpdatedAt());
        return vo;
    }


    @Override
    public String createBorrowOrders(Long itemId) {
        Long userId = UserContext.getUser();

        String lockKey = "order:lock:" + userId + ":" + itemId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = lock.tryLock();
        if (!locked) {
            log.warn("触发幂等校验，请勿重复提交订单，userId: {}, itemId: {}", userId, itemId);
            throw new BadRequestException("您已提交过该物品的购买申请，请耐心等待卖家处理");
        }

        try {
            ItemsVO item = itemClient.getById(itemId);
            if (item == null) {
                log.error("物品不存在，itemId：{}", itemId);
                throw new BadRequestException("物品不存在");
            }
            if (item.getOwnerId()
                    .equals(userId)) {
                log.error("不能购买属于自己的物品，itemId：{}", itemId);
                throw new BadRequestException("不能购买属于自己的物品");
            }
            if (ItemStatus.SOLD.getValue() == item.getStatus() || ItemStatus.OFF_SHELF.getValue() == item.getStatus()) {
                log.error("物品不可购买，itemId：{}", itemId);
                throw new BadRequestException("物品状态异常");
            }

            Boolean exists = borrowOrdersMapper.existsPendingOrder(userId, itemId);
            if (Boolean.TRUE.equals(exists)) {
                log.warn("用户已存在待确认订单，userId: {}, itemId: {}", userId, itemId);
                throw new BadRequestException("您已提交过该物品的购买申请，请耐心等待卖家处理");
            }

            BigDecimal price = item.getPrice();

            BorrowOrdersPO borrowOrdersPO = new BorrowOrdersPO();

            borrowOrdersPO.setItemId(itemId);
            borrowOrdersPO.setBuyerId(userId);
            borrowOrdersPO.setSellerId(item.getOwnerId());
            borrowOrdersPO.setTitle(item.getTitle());
            borrowOrdersPO.setPrice(price);

            borrowOrdersPO.setStatus(1);

            borrowOrdersPO.setVersion(0);
            borrowOrdersPO.setCreatedAt(System.currentTimeMillis());
            borrowOrdersPO.setUpdatedAt(System.currentTimeMillis());

            save(borrowOrdersPO);

            // 即时通知卖家：有新订单申请
            OrderNotifyMessage notifyMsg = new OrderNotifyMessage(borrowOrdersPO.getSellerId(),
                    borrowOrdersPO.getId(),
                    PURCHASE_MESSAGE.getValue());

            rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, Collections.singletonList(notifyMsg));

            // 延迟关单消息：24小时后检查状态
            rabbitMqHelper.sendDelayMessage(ORDER_DELAY_EXCHANGE,
                    ORDER_DELAY_KEY,
                    borrowOrdersPO.getId(),
                    Duration.ofDays(1L));

            return borrowOrdersPO.getId();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public PageDTO<BorrowOrdersVO> getBorrowOrdersPage(PageQuery pageQuery,
                                                       String keyword,
                                                       Integer status,
                                                       Long startTime,
                                                       Long endTime,
                                                       boolean isOut) {
        Long userId = UserContext.getUser();
        LambdaQueryChainWrapper<BorrowOrdersPO> wrapper = lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(BorrowOrdersPO::getTitle, keyword);
        }

        if (status != null && status > 0) {
            wrapper.eq(BorrowOrdersPO::getStatus, status);
        }

        if (startTime != null && startTime > 0) {
            wrapper.ge(BorrowOrdersPO::getCreatedAt, startTime);
        }

        if (endTime != null && endTime > 0) {
            wrapper.le(BorrowOrdersPO::getCreatedAt, endTime);
        }

        if (isOut) {
            wrapper.eq(BorrowOrdersPO::getSellerId, userId);
        } else {
            wrapper.eq(BorrowOrdersPO::getBuyerId, userId);
        }

        Page<BorrowOrdersPO> pageResult = wrapper.page(pageQuery.toMpPage("created_at", false));
        List<BorrowOrdersPO> records = pageResult.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }

        List<UserDTO> userList;
        if (isOut) {
            Set<Long> buyerIds = records.stream()
                    .map(BorrowOrdersPO::getBuyerId)
                    .collect(Collectors.toSet());

            userList = userClient.queryUserByIds(buyerIds);
        } else {
            Set<Long> sellerIds = records.stream()
                    .map(BorrowOrdersPO::getSellerId)
                    .collect(Collectors.toSet());

            userList = userClient.queryUserByIds(sellerIds);
        }

        Map<Long, UserDTO> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userList)) {
            userMap = userList.stream()
                    .collect(Collectors.toMap(UserDTO::getId, user -> user));
        }

        Set<Long> itemIds = records.stream()
                .map(BorrowOrdersPO::getItemId)
                .collect(Collectors.toSet());

        List<ItemsVO> itemList = new ArrayList<>();
        if (CollUtil.isNotEmpty(itemIds)) {
            itemList = itemClient.listByIds(itemIds);
        }

        Map<Long, ItemsVO> itemMap = new HashMap<>();
        if (CollUtil.isNotEmpty(itemList)) {
            itemMap = itemList.stream()
                    .collect(Collectors.toMap(ItemsVO::getId, item -> item));
        }

        Map<Long, UserDTO> finalUserMap = userMap;
        Map<Long, ItemsVO> finalItemMap = itemMap;
        List<BorrowOrdersVO> collect = records.stream()
                .map(record -> {
                    UserDTO buyer = finalUserMap.getOrDefault(record.getBuyerId(), new UserDTO());
                    UserDTO seller = finalUserMap.getOrDefault(record.getSellerId(), new UserDTO());
                    ItemsVO item = finalItemMap.getOrDefault(record.getItemId(), new ItemsVO());
                    return toVO(record, item, buyer, seller);
                })
                .collect(Collectors.toList());

        return PageDTO.of(pageResult, collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeBorrowOrders(BorrowAgreeDTO dto) {
        String id = dto.getId();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getId, id)
                .eq(BorrowOrdersPO::getStatus, 1)
                .eq(BorrowOrdersPO::getVersion, version)
                .set(BorrowOrdersPO::getStatus, 2)
                .set(BorrowOrdersPO::getVersion, version + 1)
                .set(BorrowOrdersPO::getConfirmTime, System.currentTimeMillis())
                .set(BorrowOrdersPO::getUpdatedAt, System.currentTimeMillis())
                .update();

        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }

        BorrowOrdersPO currentOrder = lambdaQuery().eq(BorrowOrdersPO::getId, id)
                .one();
        Long itemId = currentOrder.getItemId();

        boolean othersRejected = lambdaUpdate().eq(BorrowOrdersPO::getItemId, itemId)
                .eq(BorrowOrdersPO::getStatus, 1)
                .ne(BorrowOrdersPO::getId, id)
                .set(BorrowOrdersPO::getStatus, 7)
                .set(BorrowOrdersPO::getCancelReason, "该物品已被他人购买")
                .set(BorrowOrdersPO::getUpdatedAt, System.currentTimeMillis())
                .update();

        if (othersRejected) {
            log.info("物品 {} 的其余申请已自动拒绝", itemId);
        }

        itemClient.batchUpdateStatus(Collections.singletonList(itemId), ItemStatus.SOLD.getValue());

        OrderNotifyMessage notifyMsg = new OrderNotifyMessage(currentOrder.getBuyerId(),
                currentOrder.getId(),
                REVIEW_MESSAGE.getValue());

        List<BorrowOrdersPO> borrowOrdersPOS = lambdaQuery().eq(BorrowOrdersPO::getItemId, itemId)
                .eq(BorrowOrdersPO::getStatus, 7)
                .list();

        List<OrderNotifyMessage> rejectMsgList = new ArrayList<>();
        rejectMsgList.add(notifyMsg);

        if (CollUtil.isNotEmpty(borrowOrdersPOS)) {
            rejectMsgList = borrowOrdersPOS.stream()
                    .map(po -> {
                        OrderNotifyMessage orderNotifyMessage = new OrderNotifyMessage();
                        orderNotifyMessage.setUserId(po.getBuyerId());
                        orderNotifyMessage.setOrderNo(po.getId());
                        orderNotifyMessage.setType(REVIEW_MESSAGE.getValue());
                        return orderNotifyMessage;
                    })
                    .toList();
        }

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, rejectMsgList);

        // 发送支付补偿延迟消息：15分钟后查询支付宝确认支付状态
        rabbitMqHelper.sendDelayMessage(ORDER_DELAY_EXCHANGE, ORDER_PAY_DELAY_KEY, id, Duration.ofMinutes(15));
    }

    @Override
    public void rejectBorrowOrders(BorrowRejectDTO dto) {
        String orderNo = dto.getId();
        Integer version = dto.getVersion();
        String reason = dto.getReason();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getStatus, 1)
                .eq(BorrowOrdersPO::getId, orderNo)
                .eq(BorrowOrdersPO::getVersion, version)
                .set(BorrowOrdersPO::getStatus, 7)
                .set(BorrowOrdersPO::getCancelReason, reason)
                .setSql("version = version + 1")
                .update();

        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }

        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                .one();

        OrderNotifyMessage orderNotifyMessage = new OrderNotifyMessage();
        orderNotifyMessage.setUserId(ordersPO.getBuyerId());
        orderNotifyMessage.setOrderNo(ordersPO.getId());
        orderNotifyMessage.setType(REVIEW_MESSAGE.getValue());

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, Collections.singletonList(orderNotifyMessage));

    }

    @Override
    public void cancelBorrowOrders(BorrowCancelDTO dto) {
        String orderNo = dto.getId();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getVersion, version)
                .eq(BorrowOrdersPO::getId, orderNo)
                .eq(BorrowOrdersPO::getStatus, 1)
                .set(BorrowOrdersPO::getStatus, 6)
                .setSql("version = version + 1")
                .update();

        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(BorrowReturnDTO dto) {
        String orderNo = dto.getId();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getId, orderNo)
                .eq(BorrowOrdersPO::getStatus, 3)
                .eq(BorrowOrdersPO::getVersion, version)
                .set(BorrowOrdersPO::getStatus, 4)
                .set(BorrowOrdersPO::getVersion, version + 1)
                .set(BorrowOrdersPO::getUpdatedAt, System.currentTimeMillis())
                .update();

        if (!updated) {
            throw new BadRequestException("订单不存在或状态异常，请刷新页面重试");
        }

        // 发送延迟确认收货消息：超时自动确认
        rabbitMqHelper.sendDelayMessage(ORDER_DELAY_EXCHANGE, ORDER_RETURN_DELAY_KEY, orderNo, Duration.ofDays(7));
    }

    @Override
    public String payBorrowOrders(BorrowPayDTO dto) {
        String orderNo = dto.getId();
        Integer version = dto.getVersion();

        // 1. 严格校验：状态必须为 2 (待付款)
        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                .eq(BorrowOrdersPO::getStatus, 2)
                .eq(BorrowOrdersPO::getVersion, version)
                .one();

        if (ordersPO == null) {
            throw new BadRequestException("订单不存在或当前状态不可支付，请刷新页面");
        }

        // 2. 构造支付宝请求模型
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(ordersPO.getId());
        model.setTotalAmount(ordersPO.getPrice()
                .toString());
        model.setSubject("物品交易：" + ordersPO.getTitle());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 3. 设置订单超时（建议与你延迟队列时间同步）
        model.setTimeExpire(ZonedDateTime.now()
                .plusMinutes(15)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 4. 构造请求并设置同步跳转地址（不再设置异步回调地址）
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);

        if (StringUtils.hasText(alipayProperties.getReturnUrl())) {
            // 同步跳转地址
            request.setReturnUrl(alipayProperties.getReturnUrl());
        }

        try {
            // 执行请求，获取自动提交的 HTML 表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            if (response.isSuccess()) {
                log.info("生成支付表单成功，订单号：{}", orderNo);
                // 5. 启动异步轮询查询支付结果
                startPaymentPolling(orderNo);
                return response.getBody();
            } else {
                throw new BadRequestException("支付宝网关响应失败：" + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝异常", e);
            throw new BadRequestException("支付系统繁忙");
        }
    }

    /**
     * 启动异步轮询查询支付宝支付结果
     */
    private void startPaymentPolling(String orderNo) {
        CompletableFuture.runAsync(() -> {
            int maxRetries = 20;
            int intervalSeconds = 5;

            for (int i = 0; i < maxRetries; i++) {
                try {
                    TimeUnit.SECONDS.sleep(intervalSeconds);
                } catch (InterruptedException e) {
                    Thread.currentThread()
                            .interrupt();
                    log.warn("【支付轮询】订单 {} 轮询被中断", orderNo);
                    return;
                }

                log.info("【支付轮询】第 {} 次查询订单 {} 支付状态", i + 1, orderNo);

                // 先检查本地订单状态，如果已不是待付款则说明已被处理
                BorrowOrdersPO currentOrder = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                        .one();
                if (currentOrder == null || currentOrder.getStatus() != 2) {
                    log.info("【支付轮询】订单 {} 状态已变为 {}，停止轮询",
                            orderNo,
                            currentOrder != null ? currentOrder.getStatus() : "null");
                    return;
                }

                // 查询支付宝
                AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                model.setOutTradeNo(orderNo);

                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                request.setBizModel(model);

                try {
                    AlipayTradeQueryResponse response = alipayClient.execute(request);
                    if (!response.isSuccess()) {
                        if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                            log.info("【支付轮询】订单 {} 尚未在支付宝创建", orderNo);
                        } else {
                            log.warn("【支付轮询】查询接口返回错误：{}", response.getSubMsg());
                        }
                        continue;
                    }

                    String tradeStatus = response.getTradeStatus();
                    log.info("【支付轮询】订单 {} 支付宝状态：{}", orderNo, tradeStatus);

                    if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                        // 金额校验
                        double alipayAmount = Double.parseDouble(response.getTotalAmount());
                        double localAmount = currentOrder.getPrice()
                                .doubleValue();

                        if (Math.abs(alipayAmount - localAmount) > 0.01) {
                            log.error("【支付轮询】严重警告：订单 {} 金额不匹配！支付宝：{}，本地：{}",
                                    orderNo,
                                    alipayAmount,
                                    localAmount);
                            return;
                        }

                        // 更新订单状态
                        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getId, orderNo)
                                .eq(BorrowOrdersPO::getStatus, 2)
                                .set(BorrowOrdersPO::getStatus, 3)
                                .set(BorrowOrdersPO::getPayTradeNo, response.getTradeNo())
                                .set(BorrowOrdersPO::getPayTime, System.currentTimeMillis())
                                .update();

                        if (updated) {
                            log.info("【支付轮询】订单 {} 支付成功，状态已更新", orderNo);
                            // 异步更新买家和卖家统计信息
                            CompletableFuture.runAsync(() -> {
                                        BorrowOrdersPO order = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                                                .one();
                                        List<UpdateStatsDTO> updateStatsDTOs = Arrays.asList(UpdateStatsDTO.builder()
                                                        .userId(order.getBuyerId())
                                                        .type(2)
                                                        .isAdd(true)
                                                        .build(),
                                                UpdateStatsDTO.builder()
                                                        .userId(order.getSellerId())
                                                        .type(3)
                                                        .isAdd(true)
                                                        .build());
                                        rabbitMqHelper.send(USER_EXCHANGE, USER_UPDATE_ORDER_STATS, updateStatsDTOs);
                                    }, orderExecutor)
                                    .exceptionally(e -> {
                                        log.error("【支付轮询】更新用户统计信息时出错", e);
                                        return null;
                                    });
                        }
                        return;
                    } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                        log.info("【支付轮询】订单 {} 在支付宝端已关闭，停止轮询", orderNo);
                        return;
                    }
                } catch (AlipayApiException e) {
                    log.error("【支付轮询】调用支付宝查询接口异常，订单：{}", orderNo, e);
                }
            }

            log.info("【支付轮询】订单 {} 轮询次数已达上限，停止轮询，交由定时任务补偿", orderNo);
        }, orderExecutor);
    }

    @Override
    public Integer getPayStatus(String orderNo) {
        BorrowOrdersPO order = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                .one();
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }
        Long currentUserId = UserContext.getUser();
        if (!order.getBuyerId()
                .equals(currentUserId)) {
            throw new BadRequestException("无权查看该订单");
        }
        return order.getStatus();
    }

    @Override
    public void syncWithAlipay(String orderNo) {
        log.info("【支付补偿】开始主动向支付宝查询订单状态，商户订单号：{}", orderNo);

        // 1. 查询本地订单，确认是否真的需要同步（避免无效查询）
        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                .one();

        if (ordersPO == null) {
            log.warn("【支付补偿】未找到本地订单：{}", orderNo);
            return;
        }

        // 如果状态已经不是待付款（status=2），说明已经处理过了，直接返回
        if (ordersPO.getStatus() != 2) {
            log.info("【支付补偿】订单 {} 状态已为 {}，无需同步", orderNo, ordersPO.getStatus());
            return;
        }

        // 2. 构造支付宝查询请求
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);

        try {
            // 3. 执行查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                log.info("【支付补偿】支付宝响应成功，订单 {} 状态为：{}", orderNo, tradeStatus);

                // 4. 核心逻辑：只有支付成功才更新
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {

                    // 安全校验：校验支付宝返回的金额与本地订单金额是否一致
                    double alipayAmount = Double.parseDouble(response.getTotalAmount());
                    double localAmount = ordersPO.getPrice()
                            .doubleValue();

                    if (Math.abs(alipayAmount - localAmount) > 0.01) {
                        log.error("【支付补偿】严重警告：订单 {} 金额不匹配！支付宝：{}，本地：{}",
                                orderNo,
                                alipayAmount,
                                localAmount);
                        return;
                    }

                    // 5. 更新本地订单状态（使用 status=2 作为条件，确保幂等更新）
                    boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getId, orderNo)
                            .eq(BorrowOrdersPO::getStatus, 2)
                            .set(BorrowOrdersPO::getStatus, 3)
                            .set(BorrowOrdersPO::getPayTime, System.currentTimeMillis())
                            .update();

                    if (updated) {
                        log.info("【支付补偿】订单 {} 状态同步成功：待付款 -> 已付款", orderNo);
                    }
                } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                    log.info("【支付补偿】订单 {} 在支付宝端已关闭", orderNo);
                }
            } else {
                // ACQ.TRADE_NOT_EXIST 表示用户还没扫码打开过支付页，支付宝还没生成这笔交易
                if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                    log.info("【支付补偿】支付宝端尚无此订单记录：{}", orderNo);
                } else {
                    log.warn("【支付补偿】查询接口返回错误：{}", response.getSubMsg());
                }
            }
        } catch (AlipayApiException e) {
            log.error("【支付补偿】调用支付宝查询接口发生异常", e);
        }
    }

    @Override
    public BorrowOrdersVO getBorrowOrdersDetail(String orderNo) {
        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                .one();

        Long itemId = ordersPO.getItemId();

        ItemsVO itemVO = itemClient.getById(itemId);


        Long buyerId = ordersPO.getBuyerId();
        UserDTO buyerUser = userClient.queryUserById(buyerId);

        Long sellerId = ordersPO.getSellerId();
        UserDTO sellerUser = userClient.queryUserById(sellerId);

        return toVO(ordersPO, itemVO, buyerUser, sellerUser);
    }

    @Override
    public BorrowOrdersAmountVO getBorrowOrdersAmount() {
        BigDecimal totalAmount = borrowOrdersMapper.getTotalAmount();

        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        // 获取今天的开始时间戳和结束时间戳
        LocalDateTime todayStart = LocalDateTime.now()
                .with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now()
                .with(LocalTime.MAX);
        long todayStartTime = todayStart.atZone(zoneId)
                .toInstant()
                .toEpochMilli();
        long todayEndTime = todayEnd.atZone(zoneId)
                .toInstant()
                .toEpochMilli();
        BigDecimal todayAmount = borrowOrdersMapper.getTodayAmount(todayStartTime, todayEndTime);

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (todayAmount == null) {
            todayAmount = BigDecimal.ZERO;
        }

        return new BorrowOrdersAmountVO(totalAmount, todayAmount);
    }

    @Override
    public List<BorrowOrdersPieVO> getBorrowOrdersPie() {
        List<BorrowOrdersCountDTO> borrowOrdersCount = borrowOrdersMapper.getBorrowOrdersCount();

        if (CollUtil.isEmpty(borrowOrdersCount)) {
            return List.of();
        }

        Map<Integer, Long> statusCountMap = borrowOrdersCount.stream()
                .collect(Collectors.toMap(BorrowOrdersCountDTO::getStatusId, BorrowOrdersCountDTO::getCount));

        Long totalCount = lambdaQuery().count();

        OrderStatus[] values = OrderStatus.values();

        return Arrays.stream(values)
                .map(status -> {
                    Long count = statusCountMap.getOrDefault(status.getValue(), 0L);

                    BorrowOrdersPieVO borrowOrdersPieVO = new BorrowOrdersPieVO();

                    borrowOrdersPieVO.setName(status.getDesc());

                    borrowOrdersPieVO.setValue(BigDecimal.valueOf(count)
                            .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)));

                    return borrowOrdersPieVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowOrdersTrendVO> getBorrowOrdersTrend(Integer days) {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (days * 24 * 60 * 60 * 1000L);

        List<BorrowOrdersPO> borrowOrdersPOS = lambdaQuery().in(BorrowOrdersPO::getStatus, 3, 4, 5)
                .ge(BorrowOrdersPO::getCreatedAt, startTime)
                .le(BorrowOrdersPO::getCreatedAt, endTime)
                .list();

        if (CollUtil.isEmpty(borrowOrdersPOS)) {
            return List.of();
        }

        Map<LocalDate, Long> countPerDay = new HashMap<>();
        Map<LocalDate, BigDecimal> amountPerDay = new HashMap<>();

        for (BorrowOrdersPO order : borrowOrdersPOS) {
            LocalDate date = Instant.ofEpochMilli(order.getCreatedAt())
                    .atZone(zoneId)
                    .toLocalDate();

            countPerDay.merge(date, 1L, Long::sum);
            amountPerDay.merge(date, order.getPrice(), BigDecimal::add);
        }

        LocalDate startDate = Instant.ofEpochMilli(startTime)
                .atZone(zoneId)
                .toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endTime)
                .atZone(zoneId)
                .toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<BorrowOrdersTrendVO> result = new ArrayList<>();

        for (int i = 0; i <= daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);

            Long count = countPerDay.get(currentDate);
            if (count == null) {
                count = 0L;
            }

            BigDecimal amount = amountPerDay.get(currentDate);
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            BorrowOrdersTrendVO vo = new BorrowOrdersTrendVO();
            vo.setDate(currentDate);
            vo.setOrderCount(count);
            vo.setTransactionAmount(amount);

            result.add(vo);
        }

        return result;
    }

    @Override
    public PageDTO<BorrowOrdersVO> getBorrowOrdersList(PageQuery pageQuery, String keyword, Integer status) {
        LambdaQueryChainWrapper<BorrowOrdersPO> wrapper = lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(BorrowOrdersPO::getTitle, keyword);
        }

        if (status != null && status > 0) {
            wrapper.eq(BorrowOrdersPO::getStatus, status);
        }

        Page<BorrowOrdersPO> pageResult = wrapper.page(pageQuery.toMpPage("created_at", false));
        List<BorrowOrdersPO> records = pageResult.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }

        Set<Long> userIds = new HashSet<>();

        records.forEach(record -> {
            userIds.add(record.getBuyerId());
            userIds.add(record.getSellerId());
        });

        List<UserDTO> userList = userClient.queryUserByIds(userIds);

        Map<Long, UserDTO> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userList)) {
            userMap = userList.stream()
                    .collect(Collectors.toMap(UserDTO::getId, user -> user));
        }

        Set<Long> itemIds = records.stream()
                .map(BorrowOrdersPO::getItemId)
                .collect(Collectors.toSet());

        List<ItemsVO> itemList = new ArrayList<>();
        if (CollUtil.isNotEmpty(itemIds)) {
            itemList = itemClient.listByIds(itemIds);
        }

        Map<Long, ItemsVO> itemMap = new HashMap<>();
        if (CollUtil.isNotEmpty(itemList)) {
            itemMap = itemList.stream()
                    .collect(Collectors.toMap(ItemsVO::getId, item -> item));
        }

        Map<Long, UserDTO> finalUserMap = userMap;
        Map<Long, ItemsVO> finalItemMap = itemMap;
        List<BorrowOrdersVO> collect = records.stream()
                .map(record -> {
                    UserDTO buyer = finalUserMap.getOrDefault(record.getBuyerId(), new UserDTO());
                    UserDTO seller = finalUserMap.getOrDefault(record.getSellerId(), new UserDTO());
                    ItemsVO item = finalItemMap.getOrDefault(record.getItemId(), new ItemsVO());
                    return toVO(record, item, buyer, seller);
                })
                .collect(Collectors.toList());

        return PageDTO.of(pageResult, collect);
    }

    @Override
    public Long reviewOrder(OrderReviewDTO dto) {
        String orderNo = dto.getId();

        BorrowOrdersPO order = lambdaQuery().eq(BorrowOrdersPO::getId, orderNo)
                .one();
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        // 只有待评价或已完成的订单可以评价
        if (order.getStatus() != 4 && order.getStatus() != 5) {
            throw new BadRequestException("当前订单状态不可评价");
        }

        ReviewCreateDTO reviewDTO = ReviewCreateDTO.builder()
                .itemId(order.getItemId())
                .targetUserId(order.getSellerId())
                .orderId(orderNo)
                .rating(dto.getRating())
                .content(dto.getContent())
                .images(dto.getImages())
                .isAnonymous(dto.getIsAnonymous())
                .build();

        Long reviewId = reviewClient.createReview(reviewDTO);

        // 评价成功后将订单状态更新为已完成
        if (order.getStatus() == 4 && reviewId != null) {
            lambdaUpdate().eq(BorrowOrdersPO::getId, orderNo)
                    .set(BorrowOrdersPO::getStatus, 5)
                    .set(BorrowOrdersPO::getUpdatedAt, System.currentTimeMillis())
                    .update();
        }

        return reviewId;
    }

}
