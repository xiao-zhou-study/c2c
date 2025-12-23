package com.aynu.order.service.impl;

import com.aynu.order.domain.po.BorrowOrders;
import com.aynu.order.mapper.BorrowOrdersMapper;
import com.aynu.order.service.IBorrowOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Service
public class BorrowOrdersServiceImpl extends ServiceImpl<BorrowOrdersMapper, BorrowOrders> implements IBorrowOrdersService {

}
