package com.aynu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.aynu.order.domain.po.OrderLogs;
import com.aynu.order.domain.vo.BorrowOrderVO;
import com.aynu.order.enums.OrderStatus;
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
 * 借用订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrders> implements IBorrowOrdersService {

    private final IOrderLogsService orderLogsService;
    private final UserClient userClient;
    private final ItemClient itemClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO createDTO) {
        Long currentUserId = UserContext.getUser();

        ItemsVO item = itemClient.getById(createDTO.getItemId());
        if (item == null) {
            throw new BadRequestException("物品不存在");
        }

        // 校验物品状态
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BadRequestException("该物品当前不可借用");
        }

        if (item.getUserId().equals(currentUserId)) {
            throw new BadRequestException("不能借用自己发布的物品");
        }

        BigDecimal price = item.getPrice();
        BigDecimal deposit = item.getDeposit() != null ? item.getDeposit() : BigDecimal.ZERO;
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(createDTO.getBorrowDays()));

        BorrowOrders order = new BorrowOrders();
        order.setItemId(createDTO.getItemId());
        order.setBorrowerId(currentUserId);
        order.setLenderId(item.getUserId());
        order.setTitle(item.getTitle());
        order.setPrice(price);
        // 直接设置枚举对象，MyBatis Plus 会处理映射
        order.setBillingType(item.getBillingType());
        order.setDeposit(deposit);
        order.setBorrowDays(createDTO.getBorrowDays());
        order.setTotalAmount(totalAmount);
        order.setPurpose(createDTO.getPurpose());

        // 设置初始状态为：申请中
        order.setStatus(OrderStatus.APPLYING);

        long now = System.currentTimeMillis();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        save(order);
        this.addOrderLog(order.getId(), currentUserId, "提交借用申请", createDTO.getPurpose());
        return order.getId();
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

    @Override
    public boolean updateOrder(Long orderId, Map<String, Object> updates) {
        return false;
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
        // ... 其他过滤逻辑保持不变 ...
        if (itemId != null && itemId > 0) wrapper.eq(BorrowOrders::getItemId, itemId);
        if (borrowerId != null && borrowerId > 0) wrapper.eq(BorrowOrders::getBorrowerId, borrowerId);
        if (lenderId != null && lenderId > 0) wrapper.eq(BorrowOrders::getLenderId, lenderId);

        if (StrUtil.isNotBlank(type)) {
            if ("borrow".equals(type)) wrapper.eq(BorrowOrders::getBorrowerId, currentUserId);
            else if ("lend".equals(type)) wrapper.eq(BorrowOrders::getLenderId, currentUserId);
        }

        wrapper.orderByDesc(BorrowOrders::getCreatedAt);
        Page<BorrowOrders> page = wrapper.page(query.toMpPage());
        if (page.getRecords().isEmpty()) return PageDTO.empty(page);

        List<BorrowOrderVO> list = page.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return PageDTO.of(page, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(OrderActionDTO actionDTO) {
        Long currentUserId = UserContext.getUser();
        BorrowOrders order = checkOrderExists(actionDTO.getOrderId());

        // 只有申请中或已确认的订单可以取消
        if (order.getStatus() != OrderStatus.APPLYING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("当前状态不允许取消订单");
        }

        if (!order.getBorrowerId().equals(currentUserId)) {
            throw new BadRequestException("只有借用人可以取消订单");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(actionDTO.getReason());
        order.setUpdatedAt(System.currentTimeMillis());

        updateById(order);
        addOrderLog(order.getId(), currentUserId, "取消订单", actionDTO.getReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long orderId) {
        Long currentUserId = UserContext.getUser();
        BorrowOrders order = checkOrderExists(orderId);

        if (order.getStatus() != OrderStatus.APPLYING) {
            throw new BadRequestException("只有申请中的订单可以确认");
        }

        if (!order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有出借人可以确认订单");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(System.currentTimeMillis());

        updateById(order);
        addOrderLog(order.getId(), currentUserId, "确认订单", null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectOrder(OrderActionDTO actionDTO) {
        Long currentUserId = UserContext.getUser();
        BorrowOrders order = checkOrderExists(actionDTO.getOrderId());

        if (order.getStatus() != OrderStatus.APPLYING) {
            throw new BadRequestException("只有申请中的订单可以拒绝");
        }

        if (!order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有出借人可以拒绝订单");
        }

        order.setStatus(OrderStatus.REJECTED);
        order.setCancelReason(actionDTO.getReason());
        order.setUpdatedAt(System.currentTimeMillis());

        updateById(order);
        addOrderLog(order.getId(), currentUserId, "拒绝订单", actionDTO.getReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowItem(Long orderId) {
        Long currentUserId = UserContext.getUser();
        BorrowOrders order = checkOrderExists(orderId);

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("只有已确认的订单可以开始借用");
        }

        if (!order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有出借人可以确认借出");
        }

        long now = System.currentTimeMillis();
        order.setStatus(OrderStatus.BORROWING);
        order.setBorrowTime(now);
        // 计算预计归还时间
        order.setReturnTime(now + (order.getBorrowDays() * 24 * 60 * 60 * 1000L));
        order.setUpdatedAt(now);

        updateById(order);
        addOrderLog(order.getId(), currentUserId, "确认借出", null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnItem(Long orderId) {
        Long currentUserId = UserContext.getUser();
        BorrowOrders order = checkOrderExists(orderId);

        if (order.getStatus() != OrderStatus.BORROWING) {
            throw new BadRequestException("只有借用中的订单可以归还");
        }

        if (!order.getBorrowerId().equals(currentUserId) && !order.getLenderId().equals(currentUserId)) {
            throw new BadRequestException("只有相关当事人可以确认归还");
        }

        long now = System.currentTimeMillis();
        order.setStatus(OrderStatus.RETURNED);
        order.setActualReturnTime(now);
        order.setUpdatedAt(now);

        updateById(order);
        addOrderLog(order.getId(), currentUserId, "确认归还", null);
        return true;
    }

    @Override
    public Map<String, Object> getBorrowStats(Long userId) {
        if (userId == null) userId = UserContext.getUser();

        long totalBorrowed = lambdaQuery().eq(BorrowOrders::getBorrowerId, userId).count();
        long borrowing = lambdaQuery().eq(BorrowOrders::getBorrowerId, userId)
                .eq(BorrowOrders::getStatus, OrderStatus.BORROWING)
                .count();
        long returned = lambdaQuery().eq(BorrowOrders::getBorrowerId, userId)
                .eq(BorrowOrders::getStatus, OrderStatus.RETURNED)
                .count();

        long totalLent = lambdaQuery().eq(BorrowOrders::getLenderId, userId).count();
        long lending = lambdaQuery().eq(BorrowOrders::getLenderId, userId)
                .eq(BorrowOrders::getStatus, OrderStatus.BORROWING)
                .count();
        long lentReturned = lambdaQuery().eq(BorrowOrders::getLenderId, userId)
                .eq(BorrowOrders::getStatus, OrderStatus.RETURNED)
                .count();

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

    // 抽离公共的订单存在性检查
    private BorrowOrders checkOrderExists(Long orderId) {
        BorrowOrders order = getById(orderId);
        if (order == null) throw new BadRequestException("订单不存在");
        return order;
    }

    // 转换VO及批量查询用户信息逻辑保持不变...
    private BorrowOrderVO convertToVO(BorrowOrders order) {
        BorrowOrderVO vo = BeanUtil.toBean(order, BorrowOrderVO.class);
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