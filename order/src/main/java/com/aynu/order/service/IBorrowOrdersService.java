package com.aynu.order.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.OrderActionDTO;
import com.aynu.order.domain.dto.OrderCreateDTO;
import com.aynu.order.domain.po.BorrowOrders;
import com.aynu.order.domain.vo.BorrowOrderVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface IBorrowOrdersService extends IService<BorrowOrders> {

    /**
     * 创建借用订单
     * @param createDTO 创建订单DTO
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO createDTO);

    /**
     * 查询订单列表
     * @param status 订单状态
     * @param itemId 物品ID
     * @param borrowerId 借用人ID
     * @param lenderId 出借人ID
     * @param type 类型：borrow-借用 lend-借出
     * @param query 分页查询条件
     * @return 订单列表
     */
    PageDTO<BorrowOrderVO> listOrders(Integer status, Long itemId, Long borrowerId, Long lenderId, String type, PageQuery query);

    /**
     * 更新订单信息
     * @param orderId 订单ID
     * @param updates 更新内容
     * @return 是否成功
     */
    boolean updateOrder(Long orderId, Map<String, Object> updates);

    /**
     * 取消订单
     * @param actionDTO 操作DTO
     * @return 是否成功
     */
    boolean cancelOrder(OrderActionDTO actionDTO);

    /**
     * 确认订单
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean confirmOrder(Long orderId);

    /**
     * 拒绝订单
     * @param actionDTO 操作DTO
     * @return 是否成功
     */
    boolean rejectOrder(OrderActionDTO actionDTO);

    /**
     * 开始借用
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean borrowItem(Long orderId);

    /**
     * 归还物品
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean returnItem(Long orderId);

    /**
     * 获取借用统计
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getBorrowStats(Long userId);
}
