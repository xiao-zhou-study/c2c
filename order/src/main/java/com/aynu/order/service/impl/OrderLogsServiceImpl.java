package com.aynu.order.service.impl;

import com.aynu.order.domain.po.OrderLogs;
import com.aynu.order.mapper.OrderLogsMapper;
import com.aynu.order.service.IOrderLogsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单日志表，记录订单状态变更的操作日志（逻辑外键关联订单/用户表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Service
public class OrderLogsServiceImpl extends ServiceImpl<OrderLogsMapper, OrderLogs> implements IOrderLogsService {

}
