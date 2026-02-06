package com.aynu.order.controller;

import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.UserContext;
import com.aynu.order.domain.dto.OrderActionDTO;
import com.aynu.order.domain.dto.OrderCreateDTO;
import com.aynu.order.domain.po.BorrowOrders;
import com.aynu.order.domain.po.OrderLogs;
import com.aynu.order.domain.vo.BorrowOrderVO;
import com.aynu.order.service.IBorrowOrdersService;
import com.aynu.order.service.IOrderLogsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表） 前端控制器
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Api(tags = "订单管理接口")
public class BorrowOrdersController {

    private final IBorrowOrdersService borrowOrdersService;
    private final IOrderLogsService orderLogsService;
    private final UserClient userClient;

    @ApiOperation("创建借用订单")
    @PostMapping
    public Long createOrder(@Valid @RequestBody OrderCreateDTO createDTO) {
        return borrowOrdersService.createOrder(createDTO);
    }

    @ApiOperation("获取订单列表")
    @GetMapping
    public PageDTO<BorrowOrderVO> listOrders(@RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) Long itemId,
                                             @RequestParam(required = false) Long borrowerId,
                                             @RequestParam(required = false) Long lenderId,
                                             @RequestParam(required = false) String type,
                                             PageQuery query) {
        return borrowOrdersService.listOrders(status, itemId, borrowerId, lenderId, type, query);
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    public BorrowOrderVO getOrderDetail(@PathVariable("orderId") Long orderId) {
        BorrowOrders order = borrowOrdersService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return convertToVO(order);
    }

    @ApiOperation("更新订单信息")
    @PutMapping("/{orderId}")
    public Boolean updateOrder(@PathVariable("orderId") Long orderId, @RequestBody Map<String, Object> updates) {
        return borrowOrdersService.updateOrder(orderId, updates);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Boolean cancelOrder(@RequestBody OrderActionDTO actionDTO) {
        return borrowOrdersService.cancelOrder(actionDTO);
    }

    @ApiOperation("确认订单")
    @PutMapping("/confirm")
    public Boolean confirmOrder(@RequestBody Map<String, Long> request) {
        return borrowOrdersService.confirmOrder(request.get("orderId"));
    }

    @ApiOperation("拒绝订单")
    @PutMapping("/reject")
    public Boolean rejectOrder(@RequestBody OrderActionDTO actionDTO) {
        return borrowOrdersService.rejectOrder(actionDTO);
    }

    @ApiOperation("开始借用")
    @PutMapping("/borrow")
    public Boolean borrowItem(@RequestBody Map<String, Long> request) {
        return borrowOrdersService.borrowItem(request.get("orderId"));
    }

    @ApiOperation("归还物品")
    @PutMapping("/return")
    public Boolean returnItem(@RequestBody Map<String, Long> request) {
        return borrowOrdersService.returnItem(request.get("orderId"));
    }

    @ApiOperation("根据物品ID查询订单列表")
    @GetMapping("/item/{itemId}")
    public List<BorrowOrderVO> getOrdersByItemId(@PathVariable("itemId") Long itemId) {
        List<BorrowOrders> orders = borrowOrdersService.lambdaQuery().eq(BorrowOrders::getItemId, itemId).list();
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @ApiOperation("获取订单操作日志")
    @GetMapping("/{orderId}/logs")
    public List<OrderLogs> getOrderLogs(@PathVariable("orderId") Long orderId) {
        return orderLogsService.lambdaQuery()
                .eq(OrderLogs::getOrderId, orderId)
                .orderByDesc(OrderLogs::getCreatedAt)
                .list();
    }

    @ApiOperation("获取借用统计")
    @GetMapping("/stats")
    public Map<String, Object> getBorrowStats() {
        Long currentUserId = UserContext.getUser();
        return borrowOrdersService.getBorrowStats(currentUserId);
    }

    private BorrowOrderVO convertToVO(BorrowOrders order) {
        BorrowOrderVO vo = new BorrowOrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItemId());
        vo.setTitle(order.getTitle());
        vo.setBorrowerId(order.getBorrowerId());
        vo.setLenderId(order.getLenderId());
        vo.setPrice(order.getPrice());
        vo.setBillingType(order.getBillingType());
        vo.setDeposit(order.getDeposit());
        vo.setBorrowDays(order.getBorrowDays());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPurpose(order.getPurpose());
        vo.setStatus(order.getStatus());
        vo.setBorrowTime(order.getBorrowTime());
        vo.setReturnTime(order.getReturnTime());
        vo.setActualReturnTime(order.getActualReturnTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

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
}
