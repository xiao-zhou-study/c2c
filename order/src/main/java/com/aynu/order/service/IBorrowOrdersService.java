package com.aynu.order.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.order.domain.dto.OrderActionDTO;
import com.aynu.order.domain.dto.OrderCreateDTO;
import com.aynu.order.domain.po.BorrowOrders;
import com.aynu.order.domain.vo.BorrowOrderVO;
import com.aynu.order.enums.OrderStatus;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 借用订单表 服务类
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
     * @param status 订单状态 (修改为 OrderStatus 枚举)
     * @param itemId 物品ID
     * @param borrowerId 借用人ID
     * @param lenderId 出借人ID
     * @param type 类型：borrow-我借用的 lend-我借出的
     * @param query 分页查询条件
     * @return 订单分页结果
     */
    PageDTO<BorrowOrderVO> listOrders(OrderStatus status,
                                      Long itemId,
                                      Long borrowerId,
                                      Long lenderId,
                                      String type,
                                      PageQuery query);

    /**
     * 更新订单信息
     * @param orderId 订单ID
     * @param updates 更新内容
     * @return 是否成功
     */
    boolean updateOrder(Long orderId, Map<String, Object> updates);

    PageDTO<BorrowOrderVO> listOrders(Integer status,
                                      Long itemId,
                                      Long borrowerId,
                                      Long lenderId,
                                      String type,
                                      PageQuery query);

    /**
     * 取消订单 (由借用人发起)
     * @param actionDTO 操作DTO
     * @return 是否成功
     */
    boolean cancelOrder(OrderActionDTO actionDTO);

    /**
     * 确认订单 (由出借人发起)
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean confirmOrder(Long orderId);

    /**
     * 拒绝订单 (由出借人发起)
     * @param actionDTO 操作DTO
     * @return 是否成功
     */
    boolean rejectOrder(OrderActionDTO actionDTO);

    /**
     * 确认借出 (由出借人确认物品已移交)
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean borrowItem(Long orderId);

    /**
     * 确认归还 (确认物品已交还)
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean returnItem(Long orderId);

    /**
     * 获取用户借用/借出统计数据
     * @param userId 用户ID
     * @return 包含总数、借用中、已归还等数量的Map
     */
    Map<String, Object> getBorrowStats(Long userId);
}