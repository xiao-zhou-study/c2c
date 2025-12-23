package com.aynu.order.domain.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 借用订单表，存储物品租赁的订单核心信息（逻辑外键关联物品/用户表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("borrow_orders")
public class BorrowOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联物品ID（逻辑外键，关联items表id）
     */
    private Long itemId;

    /**
     * 借用人ID（逻辑外键，关联users表id）
     */
    private Long borrowerId;

    /**
     * 出借人ID（逻辑外键，关联users表id）
     */
    private Long lenderId;

    /**
     * 订单标题（对应物品标题）
     */
    private String title;

    /**
     * 租赁单价（元）
     */
    private BigDecimal price;

    /**
     * 计费类型：per_day-按天、per_week-按周、per_month-按月
     */
    private String billingType;

    /**
     * 押金金额（元）
     */
    private BigDecimal deposit;

    /**
     * 租赁天数
     */
    private Integer borrowDays;

    /**
     * 订单总金额（元）=单价×天数
     */
    private BigDecimal totalAmount;

    /**
     * 借用用途说明
     */
    private String purpose;

    /**
     * 订单状态：1-申请中 2-已确认 3-借用中 4-已归还 5-已取消 6-已拒绝
     */
    private Integer status;

    /**
     * 实际借出时间戳（毫秒级）
     */
    private Long borrowTime;

    /**
     * 预计归还时间戳（毫秒级）
     */
    private Long returnTime;

    /**
     * 实际归还时间戳（毫秒级）
     */
    private Long actualReturnTime;

    /**
     * 取消/拒绝原因
     */
    private String cancelReason;

    /**
     * 订单创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 订单更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
