package com.aynu.order.controller;


import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.*;
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
import java.util.Map;

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
     * @return 借用订单分页列表
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
     * 分页获取我借用的订单
     *
     * @param pageQuery 分页参数
     * @param keyword   关键词
     * @param status    订单状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 借用订单分页列表
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
     * @return 借用订单详情
     */
    @GetMapping("/detail")
    public BorrowOrdersVO getBorrowOrdersDetail(@RequestParam String orderNo) {
        return borrowOrdersService.getBorrowOrdersDetail(orderNo);
    }

    /**
     * 同意借用订单
     *
     * @param dto dto
     */
    @PutMapping("/agree")
    public void agreeBorrowOrders(@RequestBody BorrowAgreeDTO dto) {
        borrowOrdersService.agreeBorrowOrders(dto);
    }

    /**
     * 拒绝借用订单
     *
     * @param dto 借用订单信息
     */
    @PutMapping("/reject")
    public void rejectBorrowOrders(@RequestBody BorrowRejectDTO dto) {
        borrowOrdersService.rejectBorrowOrders(dto);
    }

    /**
     * 取消借用订单
     *
     * @param dto 借用订单信息
     */
    @PutMapping("/cancel")
    public void cancelBorrowOrders(@RequestBody BorrowCancelDTO dto) {
        borrowOrdersService.cancelBorrowOrders(dto);
    }

    /**
     * 订单付款
     */
    @PutMapping("/pay")
    public String payBorrowOrders(@RequestBody BorrowPayDTO dto) {
        return borrowOrdersService.payBorrowOrders(dto);
    }

    /**
     * 回调接口
     *
     * @param params 请求
     * @return 状态
     */
    @PostMapping("/notify")
    public String handleNotify(@RequestParam Map<String, String> params) {
        log.info("支付宝异步回调接收到参数: {}", params);
        return borrowOrdersService.handleNotify(params);
    }

    // todo： 争议订单


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
