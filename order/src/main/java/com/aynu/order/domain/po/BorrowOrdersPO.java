package com.aynu.order.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 借用订单表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("borrow_orders")
@Schema(description = "借用订单表对象")
public class BorrowOrdersPO implements Serializable {
    @Serial
    private static final long serialVersionUID = 926118239127102794L;

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID，自增")
    private Long id;

    /**
     * 订单编号（唯一商户单号）
     */
    @Schema(description = "订单编号（唯一商户单号）")
    private String orderNo;

    /**
     * 关联物品ID
     */
    @Schema(description = "关联物品ID")
    private Long itemId;

    /**
     * 借用人ID
     */
    @Schema(description = "借用人ID")
    private Long borrowerId;

    /**
     * 出借人ID
     */
    @Schema(description = "出借人ID")
    private Long lenderId;

    /**
     * 订单标题快照
     */
    @Schema(description = "订单标题快照")
    private String title;

    /**
     * 租赁单价（元）
     */
    @Schema(description = "租赁单价（元）")
    private BigDecimal price;

    /**
     * 计费类型：1-按天、2-按周、3-按月
     */
    @Schema(description = "计费类型：1-按天、2-按周、3-按月")
    private Integer billingType;

    /**
     * 押金金额（元）
     */
    @Schema(description = "押金金额（元）")
    private BigDecimal deposit;

    /**
     * 租赁天数
     */
    @Schema(description = "租赁天数")
    private BigDecimal borrowDays;

    /**
     * 订单总金额（元）=单价×天数+押金
     */
    @Schema(description = "订单总金额（元）=单价×天数+押金")
    private BigDecimal totalAmount;

    /**
     * 1-待确认, 2-待付款, 3-借用中, 4-待归还确认, 5-已完成, 6-已取消, 7-已拒绝, 8-争议中
     */
    @Schema(description = "1-待确认, 2-待付款, 3-借用中, 4-待归还确认, 5-已完成, 6-已取消, 7-已拒绝, 8-争议中")
    private Integer status;

    /**
     * 借用用途说明
     */
    @Schema(description = "借用用途说明")
    private String purpose;

    /**
     * 预约借用开始时间戳（用户选择）
     */
    @Schema(description = "预约借用开始时间戳（用户选择）")
    private Long plannedStartTime;

    /**
     * 预约借用结束时间戳（用户选择）
     */
    @Schema(description = "预约借用结束时间戳（用户选择）")
    private Long plannedEndTime;

    /**
     * 出借人确认时间戳
     */
    @Schema(description = "出借人确认时间戳")
    private Long confirmTime;

    /**
     * 支付成功时间戳
     */
    @Schema(description = "支付成功时间戳")
    private Long payTime;

    /**
     * 第三方支付流水号
     */
    @Schema(description = "第三方支付流水号")
    private String payTradeNo;

    /**
     * 物品实际交付时间戳
     */
    @Schema(description = "物品实际交付时间戳")
    private Long borrowTime;

    /**
     * 预计归还时间戳
     */
    @Schema(description = "预计归还时间戳")
    private Long expectReturnTime;

    /**
     * 实际归还时间戳
     */
    @Schema(description = "实际归还时间戳")
    private Long actualReturnTime;

    /**
     * 押金退还时间戳
     */
    @Schema(description = "押金退还时间戳")
    private Long refundTime;

    /**
     * 取消/拒绝/申诉原因
     */
    @Schema(description = "取消/拒绝/申诉原因")
    private String cancelReason;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 创建时间戳
     */
    @Schema(description = "创建时间戳")
    private Long createdAt;

    /**
     * 更新时间戳
     */
    @Schema(description = "更新时间戳")
    private Long updatedAt;

}
