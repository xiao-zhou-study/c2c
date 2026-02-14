package com.aynu.order.controller;


import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.*;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.aynu.order.service.BorrowOrdersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "借用订单表接口")
@RestController
@RequestMapping("/borrow_orders")
@RequiredArgsConstructor
@Slf4j
public class BorrowOrdersController {

    private final BorrowOrdersService borrowOrdersService;

    /**
     * 创建借用订单
     *
     * @param dto 借用订单信息
     * @return 借用订单ID
     */
    @PostMapping("/create")
    public String createBorrowOrders(@RequestBody OrderDTO dto) {
        return borrowOrdersService.createBorrowOrders(dto);
    }

    /**
     * 分页获取我借出的订单
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
     * 归还借用订单
     *
     * @param dto 借用订单信息
     */
    @PutMapping("/return")
    public void returnBorrowOrders(@RequestBody BorrowReturnDTO dto) {
        borrowOrdersService.returnBorrowOrders(dto);
    }

    /**
     * 确认归还订单
     *
     * @param dto 借用订单信息
     */
    @PutMapping("/confirm")
    public void confirmBorrowOrders(@RequestBody BorrowConfirmDTO dto) {
        borrowOrdersService.confirmBorrowOrders(dto);
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

}
