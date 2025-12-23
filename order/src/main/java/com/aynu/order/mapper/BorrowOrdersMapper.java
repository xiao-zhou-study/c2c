package com.aynu.order.mapper;

import com.aynu.order.domain.po.BorrowOrders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface BorrowOrdersMapper extends BaseMapper<BorrowOrders> {

}
