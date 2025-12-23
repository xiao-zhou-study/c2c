package com.aynu.order.mapper;

import com.aynu.order.domain.po.OrderLogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 订单日志表，记录订单状态变更的操作日志（逻辑外键关联订单/用户表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface OrderLogsMapper extends BaseMapper<OrderLogs> {

}
