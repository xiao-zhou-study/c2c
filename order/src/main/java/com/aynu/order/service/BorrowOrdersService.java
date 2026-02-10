package com.aynu.order.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.OrderDTO;
import com.aynu.order.domain.po.BorrowOrdersPO;
import com.aynu.order.domain.vo.BorrowOrdersVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BorrowOrdersService extends IService<BorrowOrdersPO> {

    String createBorrowOrders(OrderDTO dto);

    PageDTO<BorrowOrdersVO> getBorrowOrdersPage(PageQuery pageQuery,
                                                String keyword,
                                                Integer status,
                                                Long startTime,
                                                Long endTime,
                                                boolean b);

    void agreeBorrowOrders(String id);
}
