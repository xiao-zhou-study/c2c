package com.aynu.order.service;

import com.aynu.order.domain.po.BorrowOrders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface IBorrowOrdersService extends IService<BorrowOrders> {

}
