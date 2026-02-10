package com.aynu.order.controller;


import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.OrderDTO;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.aynu.order.service.BorrowOrdersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "借用订单表接口")
@RestController
@RequestMapping("/borrow_orders")
@RequiredArgsConstructor
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
     * 同意借用订单
     *
     * @param id 借用订单ID
     */
    @PutMapping("/agree")
    public void agreeBorrowOrders(@RequestParam String id) {
        borrowOrdersService.agreeBorrowOrders(id);
    }

    /**
     * 拒绝借用订单
     *
     * @param id 借用订单ID
     */
    @PutMapping("/reject")
    public void rejectBorrowOrders(@RequestParam String id, String reason) {
        borrowOrdersService.rejectBorrowOrders(id, reason);
    }

    // todo : 订单剩余接口 付款、归还、确认归还、取消订单

    // todo： 争议订单

}
