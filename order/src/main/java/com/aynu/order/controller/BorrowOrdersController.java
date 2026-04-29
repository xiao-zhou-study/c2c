package com.aynu.order.controller;


import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.BorrowAgreeDTO;
import com.aynu.order.domain.dto.BorrowCancelDTO;
import com.aynu.order.domain.dto.BorrowPayDTO;
import com.aynu.order.domain.dto.BorrowRejectDTO;
import com.aynu.order.domain.dto.BorrowReturnDTO;
import com.aynu.order.domain.dto.OrderReviewDTO;
import com.aynu.order.domain.vo.BorrowOrdersAmountVO;
import com.aynu.order.domain.vo.BorrowOrdersPieVO;
import com.aynu.order.domain.vo.BorrowOrdersTrendVO;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.aynu.order.service.BorrowOrdersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单表接口")
@RestController
@RequestMapping("/borrow_orders")
@RequiredArgsConstructor
@Slf4j
public class BorrowOrdersController {

    private final BorrowOrdersService borrowOrdersService;

    /**
     * 创建购买订单
     *
     * @param itemId 物品Id
     * @return 订单ID
     */
    @PostMapping("/create")
    public String createBorrowOrders(@RequestParam Long itemId) {
        return borrowOrdersService.createBorrowOrders(itemId);
    }

    /**
     * 分页获取我卖出的订单
     *
     * @param pageQuery 分页参数
     * @param keyword   关键词
     * @param status    订单状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 二手交易订单分页列表
     */
    @GetMapping("/page/out")
    public PageDTO<BorrowOrdersVO> getBorrowOrdersPageOut(PageQuery pageQuery,
                                                          String keyword,
                                                          Integer status,
                                                          Long startTime,
                                                          Long endTime) {
        return borrowOrdersService.getBorrowOrdersPage(pageQuery, keyword, status, startTime, endTime, true);
    }

    /**
     * 分页获取我买到的订单
     *
     * @param pageQuery 分页参数
     * @param keyword   关键词
     * @param status    订单状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 二手交易订单分页列表
     */
    @GetMapping("/page/in")
    public PageDTO<BorrowOrdersVO> getBorrowOrdersPageIn(PageQuery pageQuery,
                                                         String keyword,
                                                         Integer status,
                                                         Long startTime,
                                                         Long endTime) {
        return borrowOrdersService.getBorrowOrdersPage(pageQuery, keyword, status, startTime, endTime, false);
    }

    /**
     * 获取单个订单详情
     *
     * @param orderNo 订单编号
     * @return 二手交易订单详情
     */
    @GetMapping("/detail")
    public BorrowOrdersVO getBorrowOrdersDetail(@RequestParam String orderNo) {
        return borrowOrdersService.getBorrowOrdersDetail(orderNo);
    }

    /**
     * 同意交易订单
     *
     * @param dto dto
     */
    @PutMapping("/agree")
    public void agreeBorrowOrders(@RequestBody BorrowAgreeDTO dto) {
        borrowOrdersService.agreeBorrowOrders(dto);
    }

    /**
     * 拒绝交易订单
     *
     * @param dto 交易订单信息
     */
    @PutMapping("/reject")
    public void rejectBorrowOrders(@RequestBody BorrowRejectDTO dto) {
        borrowOrdersService.rejectBorrowOrders(dto);
    }

    /**
     * 取消交易订单
     *
     * @param dto 交易订单信息
     */
    @PutMapping("/cancel")
    public void cancelBorrowOrders(@RequestBody BorrowCancelDTO dto) {
        borrowOrdersService.cancelBorrowOrders(dto);
    }

    /**
     * 确认收货
     *
     * @param dto 订单信息
     */
    @PutMapping("/confirm")
    public void confirmReceipt(@RequestBody BorrowReturnDTO dto) {
        borrowOrdersService.confirmReceipt(dto);
    }

    /**
     * 评价订单
     *
     * @param dto 评价信息
     */
    @PostMapping("/review")
    public Long reviewOrder(@RequestBody OrderReviewDTO dto) {
        return borrowOrdersService.reviewOrder(dto);
    }

    /**
     * 订单付款
     */
    @PutMapping("/pay")
    public String payBorrowOrders(@RequestBody BorrowPayDTO dto) {
        return borrowOrdersService.payBorrowOrders(dto);
    }

    /**
     * 查询订单支付状态（前端轮询用）
     *
     * @param orderNo 订单编号
     * @return 订单状态
     */
    @GetMapping("/pay/status")
    public Integer getPayStatus(@RequestParam String orderNo) {
        return borrowOrdersService.getPayStatus(orderNo);
    }

    /**
     * 获取订单数量
     *
     * @return 订单数量
     */
    @GetMapping("/count")
    public Long getBorrowOrdersCount() {
        return borrowOrdersService.count();
    }

    /**
     * 获取订单总额以及今日交易额
     *
     * @return 订单总额以及今日交易额
     */
    @GetMapping("/amount")
    public BorrowOrdersAmountVO getBorrowOrdersAmount() {
        return borrowOrdersService.getBorrowOrdersAmount();
    }

    /**
     * 订单状态饼图
     */
    @GetMapping("/pie")
    public List<BorrowOrdersPieVO> getBorrowOrdersPie() {
        return borrowOrdersService.getBorrowOrdersPie();
    }

    /**
     * 订单趋势图
     *
     * @return 响应数据
     */
    @GetMapping("/trend")
    public List<BorrowOrdersTrendVO> getBorrowOrdersTrend(@RequestParam Integer days) {
        return borrowOrdersService.getBorrowOrdersTrend(days);
    }


    /**
     * 获取所有订单列表
     *
     * @param pageQuery 分页参数
     * @param keyword   关键字
     * @param status    订单状态
     * @return 订单列表
     */
    @GetMapping("/list")
    public PageDTO<BorrowOrdersVO> getBorrowOrdersList(PageQuery pageQuery, String keyword, Integer status) {
        return borrowOrdersService.getBorrowOrdersList(pageQuery, keyword, status);
    }


}
