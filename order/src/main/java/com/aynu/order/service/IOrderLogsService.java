package com.aynu.order.service;

import com.aynu.order.domain.po.OrderLogs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单日志表，记录订单状态变更的操作日志（逻辑外键关联订单/用户表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface IOrderLogsService extends IService<OrderLogs> {

}
