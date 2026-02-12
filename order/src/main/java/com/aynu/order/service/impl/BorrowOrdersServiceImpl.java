package com.aynu.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.aynu.api.client.item.ItemClient;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.api.enums.item.ItemStatus;
import com.aynu.common.autoconfigure.mq.RabbitMqHelper;
import com.aynu.common.domain.dto.OrderNotifyMessage;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.order.config.AlipayProperties;
import com.aynu.order.domain.dto.*;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.aynu.order.service.BorrowOrdersService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.aynu.common.constants.MqConstants.Exchange.ORDER_EXCHANGE;
import static com.aynu.common.constants.MqConstants.Key.*;
import static com.aynu.common.enums.NotifyTypeEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrdersPO> implements BorrowOrdersService {

    private final ItemClient itemClient;
    private final RabbitMqHelper rabbitMqHelper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserClient userClient;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;


    @Override
    @Transactional
    public String createBorrowOrders(OrderDTO dto) {
        Long userId = UserContext.getUser();
        Long itemId = dto.getItemId();

        String lockKey = "order:lock:" + userId + ":" + itemId;

        Boolean isFirstSubmit = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofDays(1L));

        if (Boolean.FALSE.equals(isFirstSubmit)) {
            log.warn("触发幂等校验，请勿重复提交订单，userId: {}, itemId: {}", userId, itemId);
            throw new BadRequestException("您已提交过该物品的申请，请耐心等待出借人处理");
        }

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
        long now = System.currentTimeMillis();

        if (plannedStartTime == null || plannedEndTime == null || plannedStartTime < (now - 60000) || plannedStartTime >= plannedEndTime) {
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
        borrowOrdersPO.setCreatedAt(now);
        borrowOrdersPO.setUpdatedAt(now);

        save(borrowOrdersPO);

        // 即时通知出借人：有新订单申请
        OrderNotifyMessage notifyMsg = new OrderNotifyMessage(borrowOrdersPO.getLenderId(),
                borrowOrdersPO.getOrderNo(),
                BORROW_MESSAGE.getValue());
        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, notifyMsg);

        // 延迟关单消息：24小时后检查状态
        rabbitMqHelper.sendDelayMessage(ORDER_EXCHANGE,
                ORDER_DELAY_KEY,
                borrowOrdersPO.getOrderNo(),
                Duration.ofDays(1L));

        return borrowOrdersPO.getOrderNo();
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
        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, notifyMsg);

        List<BorrowOrdersPO> borrowOrdersPOS = lambdaQuery().eq(BorrowOrdersPO::getItemId, itemId)
                .eq(BorrowOrdersPO::getStatus, 7)
                .list();

        List<OrderNotifyMessage> rejectMsgList = new ArrayList<>();

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

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, orderNotifyMessage);


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

        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, orderNotifyMessage);

        rabbitMqHelper.sendDelayMessage(ORDER_EXCHANGE, ORDER_RETURN_DELAY_KEY, orderNo, Duration.ofDays(7));

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
        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, orderNotifyMessage);
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
        // 支付成功后，支付宝服务器会异步请求这个地址，这是修改状态的唯一依据
        request.setNotifyUrl("https://api.xzxfle.top/os/borrow_orders/pay/notify");
        // 支付成功后，用户浏览器跳转的地址
        request.setReturnUrl("http://localhost:3000/pay/success");

        try {
            // 执行请求，获取自动提交的 HTML 表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            if (response.isSuccess()) {
                log.info("生成支付表单成功，订单号：{}", orderNo);
                return response.getBody(); // 返回这串 HTML 给前端
            } else {
                throw new RuntimeException("支付宝网关响应失败：" + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝异常", e);
            throw new RuntimeException("支付系统繁忙");
        }
    }

    @Override
    public String handleNotify(HttpServletRequest request) {
        // 1. 获取支付宝 Post 过来的参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        // 2. 验签 (必须通过 SDK 验签，防止伪造请求)
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            log.error("支付宝验签异常", e);
            return "fail";
        }

        if (signVerified) {
            // 3. 验签通过，检查交易状态
            String tradeStatus = params.get("trade_status");
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no"); // 支付宝流水号

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 4. 修改订单状态 (内部需判断是否已处理，保证幂等)
                lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, orderNo)
                        .eq(BorrowOrdersPO::getStatus, 2)
                        .set(BorrowOrdersPO::getStatus, 3)
                        .update();
                log.info("支付宝异步通知：订单 {} 支付成功", orderNo);
            }
            return "success";
        } else {
            log.warn("支付宝异步通知验签失败");
            return "fail";
        }
    }

}
