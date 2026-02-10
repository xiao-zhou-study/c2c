package com.aynu.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
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
import com.aynu.order.domain.dto.OrderDTO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.aynu.common.constants.MqConstants.Exchange.ORDER_EXCHANGE;
import static com.aynu.common.constants.MqConstants.Key.ORDER_DELAY_KEY;
import static com.aynu.common.constants.MqConstants.Key.ORDER_NOTIFY_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrdersPO> implements BorrowOrdersService {

    private final ItemClient itemClient;
    private final RabbitMqHelper rabbitMqHelper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserClient userClient;


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
                true);
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
    @Transactional
    public void agreeBorrowOrders(String id) {
        BorrowOrdersPO oldOrder = lambdaQuery().eq(BorrowOrdersPO::getOrderNo, id)
                .one();

        lambdaUpdate().eq(BorrowOrdersPO::getOrderNo, id)
                .eq(BorrowOrdersPO::getVersion, oldOrder.getVersion())
                .set(BorrowOrdersPO::getStatus, 2)
                .set(BorrowOrdersPO::getVersion, oldOrder.getVersion() + 1)
                .update();

        OrderNotifyMessage notifyMsg = new OrderNotifyMessage(oldOrder.getBorrowerId(), oldOrder.getOrderNo(), false);
        rabbitMqHelper.send(ORDER_EXCHANGE, ORDER_NOTIFY_KEY, notifyMsg);

    }
}
