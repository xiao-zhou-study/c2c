package com.aynu.order.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.*;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.domain.vo.BorrowOrdersAmountVO;
import com.aynu.order.domain.vo.BorrowOrdersPieVO;
import com.aynu.order.domain.vo.BorrowOrdersTrendVO;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface BorrowOrdersService extends IService<BorrowOrdersPO> {

    String createBorrowOrders(Long itemId);

    PageDTO<BorrowOrdersVO> getBorrowOrdersPage(PageQuery pageQuery,
                                                String keyword,
                                                Integer status,
                                                Long startTime,
                                                Long endTime,
                                                boolean b);

    void agreeBorrowOrders(BorrowAgreeDTO dto);

    void rejectBorrowOrders(BorrowRejectDTO dto);

    void cancelBorrowOrders(BorrowCancelDTO dto);

    void confirmReceipt(BorrowReturnDTO dto);

    String payBorrowOrders(BorrowPayDTO dto);

    Integer getPayStatus(String orderNo);

    void syncWithAlipay(String orderNo);

    BorrowOrdersVO getBorrowOrdersDetail(String orderNo);

    BorrowOrdersAmountVO getBorrowOrdersAmount();

    List<BorrowOrdersPieVO> getBorrowOrdersPie();

    List<BorrowOrdersTrendVO> getBorrowOrdersTrend(Integer days);

    PageDTO<BorrowOrdersVO> getBorrowOrdersList(PageQuery pageQuery,
                                             String keyword,
                                             Integer status);

    Long reviewOrder(OrderReviewDTO dto);
}
