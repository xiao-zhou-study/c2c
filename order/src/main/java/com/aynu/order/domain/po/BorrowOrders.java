package com.aynu.order.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 借用订单表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("borrow_orders")
@Builder
public class BorrowOrders implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联物品ID
     */
    private Long itemId;

    /**
     * 借用人ID
     */
    private Long borrowerId;

    /**
     * 出借人ID
     */
    private Long lenderId;

    /**
     * 订单标题（物品快照）
     */
    private String title;

    /**
     * 租赁单价（元）
     */
    private BigDecimal price;

    /**
     * 计费类型
     * 修改点：使用 api 模块定义的 BillingType 枚举
     */
    private Integer billingType;

    /**
     * 押金金额（元）
     */
    private BigDecimal deposit;

    /**
     * 租赁天数
     */
    private Integer borrowDays;

    /**
     * 订单总金额（元）
     */
    private BigDecimal totalAmount;

    /**
     * 借用用途说明
     */
    private String purpose;

    /**
     * 订单状态
     * 修改点：使用 OrderStatus 枚举代替 Integer，避免魔术数字
     */
    private Integer status;

    /**
     * 实际借出时间戳
     */
    private Long borrowTime;

    /**
     * 预计归还时间戳
     */
    private Long returnTime;

    /**
     * 实际归还时间戳
     */
    private Long actualReturnTime;

    /**
     * 取消/拒绝原因
     */
    private String cancelReason;

    /**
     * 订单创建时间戳
     */
    private Long createdAt;

    /**
     * 订单更新时间戳
     */
    private Long updatedAt;
}