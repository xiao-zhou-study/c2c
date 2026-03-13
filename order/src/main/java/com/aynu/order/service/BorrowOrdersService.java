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
import java.util.Map;

public interface BorrowOrdersService extends IService<BorrowOrdersPO> {

    String createBorrowOrders(OrderDTO dto);

    PageDTO<BorrowOrdersVO> getBorrowOrdersPage(PageQuery pageQuery,
                                                String keyword,
                                                Integer status,
                                                Long startTime,
                                                Long endTime,
                                                boolean b);

    void agreeBorrowOrders(BorrowAgreeDTO dto);

    void rejectBorrowOrders(BorrowRejectDTO dto);

    void cancelBorrowOrders(BorrowCancelDTO dto);

    void returnBorrowOrders(BorrowReturnDTO dto);

    void confirmBorrowOrders(BorrowConfirmDTO dto);

    String payBorrowOrders(BorrowPayDTO dto);

    String handleNotify(Map<String, String> params);

    void syncWithAlipay(String orderNo);

    BorrowOrdersVO getBorrowOrdersDetail(String orderNo);

    BorrowOrdersAmountVO getBorrowOrdersAmount();

    List<BorrowOrdersPieVO> getBorrowOrdersPie();

    List<BorrowOrdersTrendVO> getBorrowOrdersTrend(Integer days);

    PageDTO<BorrowOrdersVO> getBorrowOrdersList(PageQuery pageQuery,
                                             String keyword,
                                             Integer status);
}
