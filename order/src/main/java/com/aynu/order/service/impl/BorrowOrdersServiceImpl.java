package com.aynu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.aynu.api.client.item.ItemClient;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.order.domain.dto.OrderActionDTO;
import com.aynu.order.domain.dto.OrderCreateDTO;
import com.aynu.order.domain.po.BorrowOrders;
import com.aynu.order.domain.po.OrderLogs;
import com.aynu.order.domain.vo.BorrowOrderVO;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.aynu.order.service.IBorrowOrdersService;
import com.aynu.order.service.IOrderLogsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrders> implements IBorrowOrdersService {

    private final IOrderLogsService orderLogsService;
    private final UserClient userClient;
    private final ItemClient itemClient;

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO createDTO) {
        Long currentUserId = UserContext.getUser();

        // 获取物品信息
        ItemsVO item = itemClient.getById(createDTO.getItemId());
        if (item == null) {
            throw new BadRequestException("物品不存在");
        }

        // 不能借用自己发布的物品
        if (item.getUserId().equals(currentUserId)) {
            throw new BadRequestException("不能借用自己发布的物品");
        }

        Long lenderId = item.getUserId();
        BigDecimal price = item.getPrice();
        String billingType = item.getBillingType();
        BigDecimal deposit = item.getDeposit() != null ? item.getDeposit() : BigDecimal.ZERO;
        String title = item.getTitle();

        // 计算总金额
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(createDTO.getBorrowDays()));

        BorrowOrders order = new BorrowOrders();
        order.setItemId(createDTO.getItemId());
        order.setBorrowerId(currentUserId);
        order.setLenderId(lenderId);
        order.setTitle(title);
        order.setPrice(price);
        order.setBillingType(billingType);
        order.setDeposit(deposit);
        order.setBorrowDays(createDTO.getBorrowDays());
        order.setTotalAmount(totalAmount);
        order.setPurpose(createDTO.getPurpose());
        order.setStatus(1); // 申请中
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        save(order);

        // 记录日志
        addOrderLog(order.getId(), currentUserId, "提交借用申请", createDTO.getPurpose());

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

        var wrapper = lambdaQuery();

        if (status != null && status > 0) {
            wrapper.eq(BorrowOrders::getStatus, status);
        }

        if (itemId != null && itemId > 0) {
            wrapper.eq(BorrowOrders::getItemId, itemId);
        }

        if (borrowerId != null && borrowerId > 0) {
            wrapper.eq(BorrowOrders::getBorrowerId, borrowerId);
        }

        if (lenderId != null && lenderId > 0) {
            wrapper.eq(BorrowOrders::getLenderId, lenderId);
        }

        // 根据类型过滤
        if (StrUtil.isNotBlank(type)) {
            if ("borrow".equals(type)) {
                wrapper.eq(BorrowOrders::getBorrowerId, currentUserId);
            } else if ("lend".equals(type)) {
                wrapper.eq(BorrowOrders::getLenderId, currentUserId);
            }
        }

        wrapper.orderByDesc(BorrowOrders::getCreatedAt);

        Page<BorrowOrders> page = wrapper.page(query.toMpPage());
        List<BorrowOrders> records = page.getRecords();

        if (records.isEmpty()) {
            return PageDTO.empty(page);
        }

        // 转换为VO
        List<BorrowOrderVO> list = records.stream().map(this::convertToVO).collect(Collectors.toList());

        return PageDTO.of(page, list);
    }

    @Override
    @Transactional
    public boolean updateOrder(Long orderId, Map<String, Object> updates) {
        BorrowOrders order = getById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        if (updates.containsKey("purpose")) {
            order.setPurpose((String) updates.get("purpose"));
        }

        order.setUpdatedAt(System.currentTimeMillis());
        return updateById(order);
    }

    @Override
    @Transactional
    public boolean cancelOrder(OrderActionDTO actionDTO) {
        Long currentUserId = UserContext.getUser();

        BorrowOrders order = getById(actionDTO.getOrderId());
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        // 只有申请中或已确认的订单可以取消
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BadRequestException("当前状态不允许取消订单");
        }

        // 只有借用人可以取消
        if (!order.getBorrowerId().equals(currentUserId)) {
            throw new BadRequestException("只有借用人可以取消订单");
        }

        order.setStatus(5); // 已取消
        order.setCancelReason(actionDTO.getReason());
        order.setUpdatedAt(System.currentTimeMillis());
        updateById(order);

        addOrderLog(order.getId(), currentUserId, "取消订单", actionDTO.getReason());

        return true;
    }

    @Override
    @Transactional
    public boolean confirmOrder(Long orderId) {
        Long currentUserId = UserContext.getUser();

        BorrowOrders order = getById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        // 只有申请中的订单可以确认
        if (order.getStatus() != 1) {
            throw new BadRequestException("当前状态不允许确认订单");
        }

        // 只有出借人可以确认
        if (!order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有出借人可以确认订单");
        }

        order.setStatus(2); // 已确认
        order.setUpdatedAt(System.currentTimeMillis());
        updateById(order);

        addOrderLog(order.getId(), currentUserId, "确认订单", null);

        return true;
    }

    @Override
    @Transactional
    public boolean rejectOrder(OrderActionDTO actionDTO) {
        Long currentUserId = UserContext.getUser();

        BorrowOrders order = getById(actionDTO.getOrderId());
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        // 只有申请中的订单可以拒绝
        if (order.getStatus() != 1) {
            throw new BadRequestException("当前状态不允许拒绝订单");
        }

        // 只有出借人可以拒绝
        if (!order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有出借人可以拒绝订单");
        }

        order.setStatus(6); // 已拒绝
        order.setCancelReason(actionDTO.getReason());
        order.setUpdatedAt(System.currentTimeMillis());
        updateById(order);

        addOrderLog(order.getId(), currentUserId, "拒绝订单", actionDTO.getReason());

        return true;
    }

    @Override
    @Transactional
    public boolean borrowItem(Long orderId) {
        Long currentUserId = UserContext.getUser();

        BorrowOrders order = getById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        // 只有已确认的订单可以开始借用
        if (order.getStatus() != 2) {
            throw new BadRequestException("当前状态不允许开始借用");
        }

        // 只有出借人可以开始借用
        if (!order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有出借人可以确认借出");
        }

        long now = System.currentTimeMillis();
        order.setStatus(3); // 借用中
        order.setBorrowTime(now);
        order.setReturnTime(now + (order.getBorrowDays() * 24 * 60 * 60 * 1000L));
        order.setUpdatedAt(now);
        updateById(order);

        addOrderLog(order.getId(), currentUserId, "确认借出", null);

        return true;
    }

    @Override
    @Transactional
    public boolean returnItem(Long orderId) {
        Long currentUserId = UserContext.getUser();

        BorrowOrders order = getById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }

        // 只有借用中的订单可以归还
        if (order.getStatus() != 3) {
            throw new BadRequestException("当前状态不允许归还");
        }

        // 只借用人或出借人可以确认归还
        if (!order.getBorrowerId().equals(currentUserId) && !order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有借用人或出借人可以确认归还");
        }

        long now = System.currentTimeMillis();
        order.setStatus(4); // 已归还
        order.setActualReturnTime(now);
        order.setUpdatedAt(now);
        updateById(order);

        addOrderLog(order.getId(), currentUserId, "确认归还", null);

        return true;
    }

    @Override
    public Map<String, Object> getBorrowStats(Long userId) {
        if (userId == null) {
            userId = UserContext.getUser();
        }

        // 统计借用数据
        long totalBorrowed = lambdaQuery().eq(BorrowOrders::getBorrowerId, userId).count();

        long borrowing = lambdaQuery().eq(BorrowOrders::getBorrowerId, userId).eq(BorrowOrders::getStatus, 3).count();

        long returned = lambdaQuery().eq(BorrowOrders::getBorrowerId, userId).eq(BorrowOrders::getStatus, 4).count();

        // 统计借出数据
        long totalLent = lambdaQuery().eq(BorrowOrders::getLenderId, userId).count();

        long lending = lambdaQuery().eq(BorrowOrders::getLenderId, userId).eq(BorrowOrders::getStatus, 3).count();

        long lentReturned = lambdaQuery().eq(BorrowOrders::getLenderId, userId).eq(BorrowOrders::getStatus, 4).count();

        return Map.of("totalBorrowed",
                totalBorrowed,
                "borrowing",
                borrowing,
                "returned",
                returned,
                "totalLent",
                totalLent,
                "lending",
                lending,
                "lentReturned",
                lentReturned);
    }

    private BorrowOrderVO convertToVO(BorrowOrders order) {
        BorrowOrderVO vo = BeanUtil.toBean(order, BorrowOrderVO.class);

        // 获取用户信息
        Set<Long> userIds = new HashSet<>();
        userIds.add(order.getBorrowerId());
        userIds.add(order.getLenderId());

        List<UserDTO> users = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        UserDTO borrower = userMap.get(order.getBorrowerId());
        if (borrower != null) {
            vo.setBorrowerName(borrower.getUsername());
            vo.setBorrowerAvatar(borrower.getAvatarUrl());
        }

        UserDTO lender = userMap.get(order.getLenderId());
        if (lender != null) {
            vo.setLenderName(lender.getUsername());
            vo.setLenderAvatar(lender.getAvatarUrl());
        }

        return vo;
    }

    private void addOrderLog(Long orderId, Long operatorId, String action, String remark) {
        OrderLogs log = new OrderLogs();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setRemark(remark);
        log.setCreatedAt(System.currentTimeMillis());
        orderLogsService.save(log);
    }
}
