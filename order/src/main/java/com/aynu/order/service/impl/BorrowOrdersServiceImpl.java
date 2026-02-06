package com.aynu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aynu.api.client.item.ItemClient;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.api.enums.item.ItemStatus;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.order.domain.dto.OrderActionDTO;
import com.aynu.order.domain.dto.OrderCreateDTO;
import com.aynu.order.domain.po.BorrowOrders;
import com.aynu.order.domain.vo.BorrowOrderVO;
import com.aynu.order.enums.OrderStatus;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.aynu.order.service.IBorrowOrdersService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 借用订单服务实现类 - 重构版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrders> implements IBorrowOrdersService {

    private final UserClient userClient;
    private final ItemClient itemClient;

    private static final long DAY_MS = 24 * 60 * 60 * 1000L;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO createDTO) {
        Long currentUserId = UserContext.getUser();

        // 1. 获取并校验物品
        ItemsVO item = Optional.ofNullable(itemClient.getById(createDTO.getItemId()))
                .orElseThrow(() -> new BadRequestException("物品不存在"));

        if (!Objects.equals(item.getStatus(), ItemStatus.AVAILABLE.getValue())) {
            throw new BadRequestException("该物品当前不可借用");
        }
        if (Objects.equals(item.getUserId(), currentUserId)) {
            throw new BadRequestException("不能借用自己发布的物品");
        }

        // 2. 构建订单
        BigDecimal price = item.getPrice();
        BigDecimal deposit = Objects.requireNonNullElse(item.getDeposit(), BigDecimal.ZERO);
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(createDTO.getBorrowDays()));

        BorrowOrders order = BorrowOrders.builder()
                .itemId(createDTO.getItemId())
                .borrowerId(currentUserId)
                .lenderId(item.getUserId())
                .title(item.getTitle())
                .price(price)
                .billingType(item.getBillingType())
                .deposit(deposit)
                .borrowDays(createDTO.getBorrowDays())
                .totalAmount(totalAmount)
                .purpose(createDTO.getPurpose())
                .status(OrderStatus.APPLYING.getValue())
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        save(order);
        return order.getId();
    }

    @Override
    public PageDTO<BorrowOrderVO> listOrders(Integer status,
                                             Long itemId,
                                             Long borrowerId,
                                             Long lenderId,
                                             String type,
                                             PageQuery query) {
        Long currentUserId = UserContext.getUser();

        // 1. 分页查询 PO
        Page<BorrowOrders> page = lambdaQuery().eq(status != null && status > 0, BorrowOrders::getStatus, status)
                .eq(itemId != null && itemId > 0, BorrowOrders::getItemId, itemId)
                .eq(borrowerId != null && borrowerId > 0, BorrowOrders::getBorrowerId, borrowerId)
                .eq(lenderId != null && lenderId > 0, BorrowOrders::getLenderId, lenderId)
                .apply(StrUtil.equals(type, "borrow"), "borrower_id = {0}", currentUserId)
                .apply(StrUtil.equals(type, "lend"), "lender_id = {0}", currentUserId)
                .orderByDesc(BorrowOrders::getCreatedAt)
                .page(query.toMpPage());

        if (CollUtil.isEmpty(page.getRecords())) {
            return PageDTO.empty(page);
        }

        // 2. 批量转换 VO 并填充用户信息（解决 N+1 问题）
        return PageDTO.of(page, convertToVOList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(OrderActionDTO actionDTO) {
        BorrowOrders order = checkOrderExists(actionDTO.getOrderId());
        validateRole(order.getBorrowerId(), "只有借用人可以取消订单");

        if (!OrderStatus.canCancel(order.getStatus())) {
            throw new BadRequestException("当前状态不允许取消订单");
        }

        return updateStatus(order, OrderStatus.CANCELLED, actionDTO.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long orderId) {
        BorrowOrders order = checkOrderExists(orderId);
        validateRole(order.getLenderId(), "只有出借人可以确认订单");

        if (!Objects.equals(order.getStatus(), OrderStatus.APPLYING.getValue())) {
            throw new BadRequestException("只有申请中的订单可以确认");
        }

        return updateStatus(order, OrderStatus.CONFIRMED, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectOrder(OrderActionDTO actionDTO) {
        BorrowOrders order = checkOrderExists(actionDTO.getOrderId());
        validateRole(order.getLenderId(), "只有出借人可以拒绝订单");

        if (!Objects.equals(order.getStatus(), OrderStatus.APPLYING.getValue())) {
            throw new BadRequestException("只有申请中的订单可以拒绝");
        }

        return updateStatus(order, OrderStatus.REJECTED, actionDTO.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowItem(Long orderId) {
        BorrowOrders order = checkOrderExists(orderId);
        validateRole(order.getLenderId(), "只有出借人可以确认借出");

        if (!Objects.equals(order.getStatus(), OrderStatus.CONFIRMED.getValue())) {
            throw new BadRequestException("只有已确认的订单可以开始借用");
        }

        long now = System.currentTimeMillis();
        order.setStatus(OrderStatus.BORROWING.getValue());
        order.setBorrowTime(now);
        order.setReturnTime(now + (order.getBorrowDays() * DAY_MS));
        order.setUpdatedAt(now);

        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnItem(Long orderId) {
        BorrowOrders order = checkOrderExists(orderId);
        Long currentUserId = UserContext.getUser();

        if (!Objects.equals(currentUserId, order.getBorrowerId()) && !Objects.equals(currentUserId,
                order.getLenderId())) {
            throw new BadRequestException("只有相关当事人可以确认归还");
        }

        if (!Objects.equals(order.getStatus(), OrderStatus.BORROWING.getValue())) {
            throw new BadRequestException("只有借用中的订单可以归还");
        }

        order.setStatus(OrderStatus.RETURNED.getValue());
        order.setActualReturnTime(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        return updateById(order);
    }

    @Override
    public Map<String, Object> getBorrowStats(Long userId) {
        final Long targetUid = userId != null ? userId : UserContext.getUser();

        // 优化：建议在数据库层面使用分组查询优化，此处保持逻辑但简化写法
        return Map.of("totalBorrowed",
                getCount(targetUid, true, null),
                "borrowing",
                getCount(targetUid, true, OrderStatus.BORROWING),
                "returned",
                getCount(targetUid, true, OrderStatus.RETURNED),
                "totalLent",
                getCount(targetUid, false, null),
                "lending",
                getCount(targetUid, false, OrderStatus.BORROWING),
                "lentReturned",
                getCount(targetUid, false, OrderStatus.RETURNED));
    }

    // --- 私有辅助方法 ---

    private long getCount(Long userId, boolean isBorrower, OrderStatus status) {
        return lambdaQuery().eq(isBorrower, BorrowOrders::getBorrowerId, userId)
                .eq(!isBorrower, BorrowOrders::getLenderId, userId)
                .eq(status != null, BorrowOrders::getStatus, status != null ? status.getValue() : null)
                .count();
    }

    private void validateRole(Long ownerId, String errorMsg) {
        if (!Objects.equals(UserContext.getUser(), ownerId)) {
            throw new BadRequestException(errorMsg);
        }
    }

    private boolean updateStatus(BorrowOrders order, OrderStatus status, String reason) {
        order.setStatus(status.getValue());
        if (reason != null) order.setCancelReason(reason);
        order.setUpdatedAt(System.currentTimeMillis());
        return updateById(order);
    }

    private BorrowOrders checkOrderExists(Long orderId) {
        return Optional.ofNullable(getById(orderId))
                .orElseThrow(() -> new BadRequestException("订单不存在"));
    }

    private List<BorrowOrderVO> convertToVOList(List<BorrowOrders> orders) {
        // 1. 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        orders.forEach(o -> {
            userIds.add(o.getBorrowerId());
            userIds.add(o.getLenderId());
        });

        // 2. 批量一次性查询用户
        Map<Long, UserDTO> userMap = userClient.queryUserByIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserDTO::getId, Function.identity(), (a, b) -> a));

        // 3. 组装 VO
        return orders.stream()
                .map(order -> {
                    BorrowOrderVO vo = BeanUtil.toBean(order, BorrowOrderVO.class);
                    Optional.ofNullable(userMap.get(order.getBorrowerId()))
                            .ifPresent(u -> {
                                vo.setBorrowerName(u.getUsername());
                                vo.setBorrowerAvatar(u.getAvatarUrl());
                            });
                    Optional.ofNullable(userMap.get(order.getLenderId()))
                            .ifPresent(u -> {
                                vo.setLenderName(u.getUsername());
                                vo.setLenderAvatar(u.getAvatarUrl());
                            });
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // 占位实现，防止接口报错
    @Override
    public boolean updateOrder(Long orderId, Map<String, Object> updates) {
        return false;
    }

    @Override
    public PageDTO<BorrowOrderVO> listOrders(OrderStatus status,
                                             Long itemId,
                                             Long borrowerId,
                                             Long lenderId,
                                             String type,
                                             PageQuery query) {
        return null;
    }
}