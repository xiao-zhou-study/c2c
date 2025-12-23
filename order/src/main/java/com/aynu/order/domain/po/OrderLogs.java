package com.aynu.order.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单日志表，记录订单状态变更的操作日志（逻辑外键关联订单/用户表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_logs")
public class OrderLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID（逻辑外键，关联borrow_orders表id）
     */
    private Long orderId;

    /**
     * 操作人ID（逻辑外键，关联users表id）
     */
    private Long operatorId;

    /**
     * 操作行为（如“提交申请”“确认订单”“取消订单”等）
     */
    private String action;

    /**
     * 操作备注/说明
     */
    private String remark;

    /**
     * 日志创建时间戳（毫秒级）
     */
    private Long createdAt;


}
