package com.aynu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.aynu.api.client.item.ItemClient;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
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
import java.util.stream.Collectors;

import static com.aynu.common.constants.MqConstants.Exchange.*;
import static com.aynu.common.constants.MqConstants.Key.*;
import static com.aynu.common.enums.NotifyTypeEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrdersPO> implements BorrowOrdersService {

    private final ItemClient itemClient;
    private final RabbitMqHelper rabbitMqHelper;
    private final UserClient userClient;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final RedissonClient redissonClient;
    private final BorrowOrdersMapper borrowOrdersMapper;

    @Resource(name = "orderExecutor")
    private final Executor orderExecutor;


    @Override
    @Transactional
    public String createBorrowOrders(OrderDTO dto) {
        Long userId = UserContext.getUser();
        Long itemId = dto.getItemId();

        String lockKey = "order:lock:" + userId + ":" + itemId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = lock.tryLock();
        if (!locked) {
            log.warn("触发幂等校验，请勿重复提交订单，userId: {}, itemId: {}", userId, itemId);
            throw new BadRequestException("您已提交过该物品的申请，请耐心等待出借人处理");
        }

        try {
            ItemsVO item = itemClient.getById(itemId);
            if (item == null) {
                log.error("物品不存在，itemId：{}", itemId);
                throw new BadRequestException("物品不存在");
            }
            if (item.getOwnerId()
                    .equals(userId)) {
                log.error("不能借用属于自己的物品，itemId：{}", itemId);
                throw new BadRequestException("不能借用属于自己的物品");
            }
            if (ItemStatus.AVAILABLE.getValue() != item.getStatus()) {
                log.error("物品不可借用，itemId：{}", itemId);
                throw new BadRequestException("物品状态异常");
            }

            Long plannedStartTime = dto.getPlannedStartTime();
            Long plannedEndTime = dto.getPlannedEndTime();
            long todayStartTime = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            if (plannedStartTime == null || plannedEndTime == null || plannedStartTime < todayStartTime || plannedStartTime >= plannedEndTime) {
                log.error("借用时间异常，开始时间：{}，结束时间：{}，itemId：{}", plannedStartTime, plannedEndTime, itemId);
                throw new BadRequestException("借用时间参数不合法");
            }

            long diffMillis = plannedEndTime - plannedStartTime;
            BigDecimal diffMillisBd = BigDecimal.valueOf(diffMillis);

            BigDecimal millisPerDay = new BigDecimal("86400000");
            BigDecimal millisPerWeek = new BigDecimal("604800000");
            BigDecimal millisPerMonth = new BigDecimal("2592000000");

            Integer billingType = item.getBillingType();
            BigDecimal duration = switch (billingType) {
                case 1 -> diffMillisBd.divide(millisPerDay, 2, RoundingMode.HALF_UP);
                case 2 -> diffMillisBd.divide(millisPerWeek, 2, RoundingMode.HALF_UP);
                case 3 -> diffMillisBd.divide(millisPerMonth, 2, RoundingMode.HALF_UP);
                default -> throw new BadRequestException("未知的计费类型");
            };

            BigDecimal price = item.getPrice();
            BigDecimal deposit = item.getDeposit();
            BigDecimal totalAmount = price.multiply(duration)
                    .add(deposit)
                    .setScale(2, RoundingMode.HALF_UP);

            BorrowOrdersPO borrowOrdersPO = new BorrowOrdersPO();

            borrowOrdersPO.setOrderNo(IdUtil.getSnowflake()
                    .nextIdStr());

            borrowOrdersPO.setItemId(itemId);
            borrowOrdersPO.setBorrowerId(userId);
            borrowOrdersPO.setLenderId(item.getOwnerId());
            borrowOrdersPO.setTitle(item.getTitle());
            borrowOrdersPO.setPrice(price);
            borrowOrdersPO.setBillingType(billingType);
            borrowOrdersPO.setDeposit(deposit);

            borrowOrdersPO.setBorrowDays(duration);
            borrowOrdersPO.setTotalAmount(totalAmount);

            borrowOrdersPO.setStatus(1);
            borrowOrdersPO.setPurpose(dto.getPurpose());
            borrowOrdersPO.setPlannedStartTime(plannedStartTime);
            borrowOrdersPO.setPlannedEndTime(plannedEndTime);

            borrowOrdersPO.setVersion(0);
            borrowOrdersPO.setCreatedAt(System.currentTimeMillis());
            borrowOrdersPO.setUpdatedAt(System.currentTimeMillis());

            save(borrowOrdersPO);

            // 即时通知出借人：有新订单申请
            OrderNotifyMessage notifyMsg = new OrderNotifyMessage(borrowOrdersPO.getLenderId(),
                    borrowOrdersPO.getOrderNo(),
                    BORROW_MESSAGE.getValue());
            rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, Collections.singletonList(notifyMsg));

            // 延迟关单消息：24小时后检查状态
            rabbitMqHelper.sendDelayMessage(ORDER_DELAY_EXCHANGE,
                    ORDER_DELAY_KEY,
                    borrowOrdersPO.getOrderNo(),
                    Duration.ofDays(1L));

            return borrowOrdersPO.getOrderNo();
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
            wrapper.eq(BorrowOrdersPO::getLenderId, userId);
        } else {
            wrapper.eq(BorrowOrdersPO::getBorrowerId, userId);
        }

        Page<BorrowOrdersPO> pageResult = wrapper.page(pageQuery.toMpPage("created_at", false));
        List<BorrowOrdersPO> records = pageResult.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(pageResult);
        }

        List<UserDTO> userList;
        if (isOut) {
            Set<Long> borrowerIds = records.stream()
                    .map(BorrowOrdersPO::getBorrowerId)
                    .collect(Collectors.toSet());

            userList = userClient.queryUserByIds(borrowerIds);
        } else {
            Set<Long> lenderIds = records.stream()
                    .map(BorrowOrdersPO::getLenderId)
                    .collect(Collectors.toSet());

            userList = userClient.queryUserByIds(lenderIds);
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
                    UserDTO user;
                    if (isOut) {
                        user = finalUserMap.getOrDefault(record.getBorrowerId(), new UserDTO());
                    } else {
                        user = finalUserMap.getOrDefault(record.getLenderId(), new UserDTO());
                    }
                    ItemsVO item = finalItemMap.getOrDefault(record.getItemId(), new ItemsVO());

                    BorrowOrdersVO borrowOrdersVO = new BorrowOrdersVO();
                    borrowOrdersVO.setId(record.getId());
                    borrowOrdersVO.setOrderNo(record.getOrderNo());
                    borrowOrdersVO.setItemId(record.getItemId());
                    borrowOrdersVO.setItemName(item.getTitle());
                    borrowOrdersVO.setItemImages(item.getImages());
                    borrowOrdersVO.setBorrowerId(record.getBorrowerId());

                    if (isOut) {
                        borrowOrdersVO.setBorrowerName(user.getUsername());
                        borrowOrdersVO.setBorrowerAvatar(user.getAvatarUrl());
                    } else {
                        borrowOrdersVO.setLenderName(user.getUsername());
                        borrowOrdersVO.setLenderAvatar(user.getAvatarUrl());
                    }

                    borrowOrdersVO.setLenderId(record.getLenderId());
                    borrowOrdersVO.setTitle(record.getTitle());
                    borrowOrdersVO.setPrice(record.getPrice());
                    borrowOrdersVO.setBillingType(record.getBillingType());
                    borrowOrdersVO.setDeposit(record.getDeposit());
                    borrowOrdersVO.setBorrowDays(record.getBorrowDays());
                    borrowOrdersVO.setTotalAmount(record.getTotalAmount());
                    borrowOrdersVO.setStatus(record.getStatus());
                    borrowOrdersVO.setPurpose(record.getPurpose());
                    borrowOrdersVO.setPlannedStartTime(record.getPlannedStartTime());
                    borrowOrdersVO.setPlannedEndTime(record.getPlannedEndTime());
                    borrowOrdersVO.setConfirmTime(record.getConfirmTime());
                    borrowOrdersVO.setPayTime(record.getPayTime());
                    borrowOrdersVO.setPayTradeNo(record.getPayTradeNo());
                    borrowOrdersVO.setBorrowTime(record.getBorrowTime());
                    borrowOrdersVO.setExpectReturnTime(record.getExpectReturnTime());
                    borrowOrdersVO.setActualReturnTime(record.getActualReturnTime());
                    borrowOrdersVO.setRefundTime(record.getRefundTime());
                    borrowOrdersVO.setCancelReason(record.getCancelReason());
                    borrowOrdersVO.setVersion(record.getVersion());
                    borrowOrdersVO.setCreatedAt(record.getCreatedAt());
                    borrowOrdersVO.setUpdatedAt(record.getUpdatedAt());

                    return borrowOrdersVO;
                })
                .collect(Collectors.toList());

        return PageDTO.of(pageResult, collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeBorrowOrders(BorrowAgreeDTO dto) {
        String id = dto.getId();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, id)
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

        BorrowOrdersPO currentOrder = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, id)
                .one();
        Long itemId = currentOrder.getItemId();

        boolean othersRejected = lambdaUpdate().eq(BorrowOrdersPO::getItemId, itemId)
                .eq(BorrowOrdersPO::getStatus, 1)
                .ne(BorrowOrdersPO::getOrderNo, id)
                .set(BorrowOrdersPO::getStatus, 7)
                .set(BorrowOrdersPO::getCancelReason, "该物品已被他人借用")
                .set(BorrowOrdersPO::getUpdatedAt, System.currentTimeMillis())
                .update();

        if (othersRejected) {
            log.info("物品 {} 的其余申请已自动拒绝", itemId);
        }

        itemClient.batchUpdateStatus(Collections.singletonList(itemId), ItemStatus.BORROWED.getValue());

        OrderNotifyMessage notifyMsg = new OrderNotifyMessage(currentOrder.getBorrowerId(),
                currentOrder.getOrderNo(),
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
                        orderNotifyMessage.setUserId(po.getBorrowerId());
                        orderNotifyMessage.setOrderNo(po.getOrderNo());
                        orderNotifyMessage.setType(REVIEW_MESSAGE.getValue());
                        return orderNotifyMessage;
                    })
                    .toList();
        }

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, rejectMsgList);

    }

    @Override
    public void rejectBorrowOrders(BorrowRejectDTO dto) {
        String orderNo = dto.getId();
        Integer version = dto.getVersion();
        String reason = dto.getReason();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getStatus, 1)
                .eq(BorrowOrdersPO::getOrderNo, orderNo)
                .eq(BorrowOrdersPO::getVersion, version)
                .set(BorrowOrdersPO::getStatus, 7)
                .set(BorrowOrdersPO::getCancelReason, reason)
                .setSql("version = version + 1")
                .update();

        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }

        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .one();

        OrderNotifyMessage orderNotifyMessage = new OrderNotifyMessage();
        orderNotifyMessage.setUserId(ordersPO.getBorrowerId());
        orderNotifyMessage.setOrderNo(ordersPO.getOrderNo());
        orderNotifyMessage.setType(REVIEW_MESSAGE.getValue());

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, Collections.singletonList(orderNotifyMessage));

    }

    @Override
    public void cancelBorrowOrders(BorrowCancelDTO dto) {
        String orderNo = dto.getOrderNo();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getVersion, version)
                .eq(BorrowOrdersPO::getOrderNo, orderNo)
                .eq(BorrowOrdersPO::getStatus, 1)
                .set(BorrowOrdersPO::getStatus, 6)
                .setSql("version = version + 1")
                .update();

        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }
    }

    @Override
    public void returnBorrowOrders(BorrowReturnDTO dto) {
        String orderNo = dto.getOrderNo();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .eq(BorrowOrdersPO::getStatus, 3)
                .eq(BorrowOrdersPO::getVersion, version)
                .set(BorrowOrdersPO::getStatus, 4)
                .setSql("version = version + 1")
                .update();

        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }

        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .one();

        OrderNotifyMessage orderNotifyMessage = new OrderNotifyMessage(ordersPO.getLenderId(),
                orderNo,
                RETURN_MESSAGE.getValue());

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, Collections.singletonList(orderNotifyMessage));

        rabbitMqHelper.sendDelayMessage(ORDER_DELAY_EXCHANGE, ORDER_RETURN_DELAY_KEY, orderNo, Duration.ofDays(7));

    }

    @Override
    @Transactional
    public void confirmBorrowOrders(BorrowConfirmDTO dto) {
        String orderNo = dto.getOrderNo();
        Integer version = dto.getVersion();

        boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .eq(BorrowOrdersPO::getStatus, 4)
                .eq(BorrowOrdersPO::getVersion, version)
                .set(BorrowOrdersPO::getStatus, 5)
                .setSql("version = version + 1")
                .update();
        if (!updated) {
            throw new BadRequestException("订单已被他人处理或数据已过期，请刷新页面重试");
        }

        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .one();

        // todo 退还押金

        // 更改物品状态
        itemClient.batchUpdateStatus(Collections.singletonList(ordersPO.getItemId()), ItemStatus.AVAILABLE.getValue());

        // todo 给物品所有人打款

        // 创建消息
        OrderNotifyMessage orderNotifyMessage = new OrderNotifyMessage(ordersPO.getBorrowerId(),
                orderNo,
                RETURN_MESSAGE.getValue());
        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, Collections.singletonList(orderNotifyMessage));
    }

    @Override
    public String payBorrowOrders(BorrowPayDTO dto) {
        String orderNo = dto.getOrderNo();
        Integer version = dto.getVersion();

        // 1. 严格校验：状态必须为 2 (待付款)
        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .eq(BorrowOrdersPO::getStatus, 2)
                .eq(BorrowOrdersPO::getVersion, version)
                .one();

        if (ordersPO == null) {
            throw new BadRequestException("订单不存在或当前状态不可支付，请刷新页面");
        }

        // 2. 构造支付宝请求模型
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(ordersPO.getOrderNo());
        model.setTotalAmount(ordersPO.getTotalAmount()
                .toString());
        model.setSubject("物品租赁：" + ordersPO.getTitle());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 3. 设置订单超时（建议与你延迟队列时间同步）
        model.setTimeExpire(ZonedDateTime.now()
                .plusMinutes(15)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 4. 构造请求并设置回调地址
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);

        if (StringUtils.hasText(alipayProperties.getNotifyUrl())) {
            //异步接收地址，公网可访问
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
        }
        if (StringUtils.hasText(alipayProperties.getReturnUrl())) {
            //同步跳转地址
            request.setReturnUrl(alipayProperties.getReturnUrl());
        }

        try {
            // 执行请求，获取自动提交的 HTML 表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            if (response.isSuccess()) {
                log.info("生成支付表单成功，订单号：{}", orderNo);
                return response.getBody();
            } else {
                throw new BadRequestException("支付宝网关响应失败：" + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝异常", e);
            throw new BadRequestException("支付系统繁忙");
        }
    }

    @Override
    public String handleNotify(Map<String, String> params) {
        // 1. 记录日志 (直接使用传入的 params)
        log.info("【支付宝回调】开始处理。收到参数个数: {}", params.size());

        // 调试专用：打印出接收到的签名
        log.debug("【支付宝回调】解析后的参数 Map: {}, sign: {}", params, params.get("sign"));

        if (params.isEmpty()) {
            log.error("【支付宝回调】异常：参数 Map 为空，请检查网关是否截断了 POST Body");
            return "fail";
        }

        // 2. 验签 (必须通过 SDK 验签)
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType());
        } catch (AlipayApiException e) {
            log.error("【支付宝回调】验签系统异常", e);
            return "fail";
        }

        if (!signVerified) {
            log.warn("【支付宝回调】验签失败！参数: {}", params);
            return "fail";
        }

        // 3. 验签通过，处理业务逻辑
        String tradeStatus = params.get("trade_status");
        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        log.info("【支付宝回调】验签通过。订单号: {}, 交易状态: {}, 支付宝交易号: {}", orderNo, tradeStatus, tradeNo);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 4. 修改订单状态 (status=2 确保幂等)，同时保存支付宝交易号
            boolean updateResult = lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, orderNo)
                    .eq(BorrowOrdersPO::getStatus, 2)
                    .set(BorrowOrdersPO::getStatus, 3)
                    .set(BorrowOrdersPO::getPayTradeNo, tradeNo)
                    .update();

            if (updateResult) {
                log.info("【支付宝回调】订单 {} 状态更新成功 -> 已支付", orderNo);
            } else {
                log.info("【支付宝回调】订单 {} 状态已在之前更新过，跳过本次处理", orderNo);
            }

            // 异步更新借用者以及出借人的统计信息（已借出数量和借用中数量）
            CompletableFuture.runAsync(() -> {
                        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
                                .one();

                        List<UpdateStatsDTO> updateStatsDTOs = Arrays.asList(UpdateStatsDTO.builder()
                                        .userId(ordersPO.getBorrowerId())
                                        .type(2)
                                        .isAdd(true)
                                        .build(),
                                UpdateStatsDTO.builder()
                                        .userId(ordersPO.getLenderId())
                                        .type(3)
                                        .isAdd(true)
                                        .build());
                        rabbitMqHelper.send(USER_EXCHANGE, USER_UPDATE_ORDER_STATS, updateStatsDTOs);
                    }, orderExecutor)
                    .exceptionally(e -> {
                        log.error("【支付宝回调】更新用户统计信息时出错", e);
                        return null;
                    });
        }

        // 5. 返回 success 给支付宝
        return "success";
    }

    @Override
    public void syncWithAlipay(String orderNo) {
        log.info("【支付补偿】开始主动向支付宝查询订单状态，商户订单号：{}", orderNo);

        // 1. 查询本地订单，确认是否真的需要同步（避免无效查询）
        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
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
                    double localAmount = ordersPO.getTotalAmount()
                            .doubleValue();

                    if (Math.abs(alipayAmount - localAmount) > 0.01) {
                        log.error("【支付补偿】严重警告：订单 {} 金额不匹配！支付宝：{}，本地：{}",
                                orderNo,
                                alipayAmount,
                                localAmount);
                        return;
                    }

                    // 5. 更新本地订单状态（使用 status=2 作为条件，确保幂等更新）
                    boolean updated = lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, orderNo)
                            .eq(BorrowOrdersPO::getStatus, 2)
                            .set(BorrowOrdersPO::getStatus, 3)
                            .update();

                    if (updated) {
                        log.info("【支付补偿】订单 {} 状态同步成功：待付款 -> 已付款", orderNo);
                    }
                } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                    log.info("【支付补偿】订单 {} 在支付宝端已关闭", orderNo);
//                    lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, orderNo)
//                            .eq(BorrowOrdersPO::getStatus, 2)
//                            .set(BorrowOrdersPO::getStatus, 5) // 假设 5 是已关闭
//                            .update();
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
        BorrowOrdersPO ordersPO = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, orderNo)
                .one();

        Long itemId = ordersPO.getItemId();

        ItemsVO itemVO = itemClient.getById(itemId);


        Long borrowerId = ordersPO.getBorrowerId();

        UserDTO borrowUser = userClient.queryUserById(borrowerId);

        Long lenderId = ordersPO.getLenderId();

        UserDTO lenderUser = userClient.queryUserById(lenderId);

        BorrowOrdersVO borrowOrdersVO = BeanUtil.toBean(ordersPO, BorrowOrdersVO.class);
        borrowOrdersVO.setItemImages(itemVO.getImages());
        borrowOrdersVO.setItemName(itemVO.getTitle());
        borrowOrdersVO.setBorrowerName(borrowUser.getUsername());
        borrowOrdersVO.setBorrowerAvatar(borrowUser.getAvatarUrl());
        borrowOrdersVO.setLenderName(lenderUser.getUsername());
        borrowOrdersVO.setLenderAvatar(lenderUser.getAvatarUrl());

        return borrowOrdersVO;
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

        List<BorrowOrdersPO> borrowOrdersPOS = lambdaQuery().in(BorrowOrdersPO::getStatus, 2, 3, 4, 5)
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
            amountPerDay.merge(date, order.getTotalAmount(), BigDecimal::add);
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
            userIds.add(record.getBorrowerId());
            userIds.add(record.getLenderId());
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
                    UserDTO userBorrower;
                    UserDTO userLender;
                    userBorrower = finalUserMap.getOrDefault(record.getBorrowerId(), new UserDTO());
                    userLender = finalUserMap.getOrDefault(record.getLenderId(), new UserDTO());
                    ItemsVO item = finalItemMap.getOrDefault(record.getItemId(), new ItemsVO());

                    BorrowOrdersVO borrowOrdersVO = new BorrowOrdersVO();
                    borrowOrdersVO.setId(record.getId());
                    borrowOrdersVO.setOrderNo(record.getOrderNo());
                    borrowOrdersVO.setItemId(record.getItemId());
                    borrowOrdersVO.setItemName(item.getTitle());
                    borrowOrdersVO.setItemImages(item.getImages());
                    borrowOrdersVO.setBorrowerId(record.getBorrowerId());
                    borrowOrdersVO.setBorrowerName(userBorrower.getUsername());
                    borrowOrdersVO.setBorrowerAvatar(userBorrower.getAvatarUrl());
                    borrowOrdersVO.setLenderName(userLender.getUsername());
                    borrowOrdersVO.setLenderAvatar(userLender.getAvatarUrl());
                    borrowOrdersVO.setLenderId(record.getLenderId());
                    borrowOrdersVO.setTitle(record.getTitle());
                    borrowOrdersVO.setPrice(record.getPrice());
                    borrowOrdersVO.setBillingType(record.getBillingType());
                    borrowOrdersVO.setDeposit(record.getDeposit());
                    borrowOrdersVO.setBorrowDays(record.getBorrowDays());
                    borrowOrdersVO.setTotalAmount(record.getTotalAmount());
                    borrowOrdersVO.setStatus(record.getStatus());
                    borrowOrdersVO.setPurpose(record.getPurpose());
                    borrowOrdersVO.setPlannedStartTime(record.getPlannedStartTime());
                    borrowOrdersVO.setPlannedEndTime(record.getPlannedEndTime());
                    borrowOrdersVO.setConfirmTime(record.getConfirmTime());
                    borrowOrdersVO.setPayTime(record.getPayTime());
                    borrowOrdersVO.setPayTradeNo(record.getPayTradeNo());
                    borrowOrdersVO.setBorrowTime(record.getBorrowTime());
                    borrowOrdersVO.setExpectReturnTime(record.getExpectReturnTime());
                    borrowOrdersVO.setActualReturnTime(record.getActualReturnTime());
                    borrowOrdersVO.setRefundTime(record.getRefundTime());
                    borrowOrdersVO.setCancelReason(record.getCancelReason());
                    borrowOrdersVO.setVersion(record.getVersion());
                    borrowOrdersVO.setCreatedAt(record.getCreatedAt());
                    borrowOrdersVO.setUpdatedAt(record.getUpdatedAt());

                    return borrowOrdersVO;
                })
                .collect(Collectors.toList());

        return PageDTO.of(pageResult, collect);
    }

}
