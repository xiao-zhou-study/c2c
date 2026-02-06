package com.aynu.order.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "订单详情VO")
public class BorrowOrderVO {

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "物品ID")
    private Long itemId;

    @ApiModelProperty(value = "物品标题")
    private String title;

    @ApiModelProperty(value = "借用人ID")
    private Long borrowerId;

    @ApiModelProperty(value = "借用人用户名")
    private String borrowerName;

    @ApiModelProperty(value = "借用人头像")
    private String borrowerAvatar;

    @ApiModelProperty(value = "出借人ID")
    private Long lenderId;

    @ApiModelProperty(value = "出借人用户名")
    private String lenderName;

    @ApiModelProperty(value = "出借人头像")
    private String lenderAvatar;

    @ApiModelProperty(value = "租赁单价")
    private BigDecimal price;

    /**
     * 计费类型枚举
     * 修改点：使用 BillingType 枚举，Jackson 会根据 @JsonValue 序列化为数字或指定格式
     */
    @ApiModelProperty(value = "计费类型：1-按天、2-按周、3-按月")
    private Integer billingType;

    @ApiModelProperty(value = "押金金额")
    private BigDecimal deposit;

    @ApiModelProperty(value = "租赁天数")
    private Integer borrowDays;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "借用用途说明")
    private String purpose;

    /**
     * 订单状态枚举
     * 修改点：使用 OrderStatus 枚举，前端可直接获取状态 Code 和 描述
     */
    @ApiModelProperty(value = "订单状态：1-申请中 2-已确认 3-借用中 4-已归还 5-已取消 6-已拒绝")
    private Integer status;

    @ApiModelProperty(value = "实际借出时间戳")
    private Long borrowTime;

    @ApiModelProperty(value = "预计归还时间戳")
    private Long returnTime;

    @ApiModelProperty(value = "实际归还时间戳")
    private Long actualReturnTime;

    @ApiModelProperty(value = "取消/拒绝原因")
    private String cancelReason;

    @ApiModelProperty(value = "订单创建时间戳")
    private Long createdAt;

    @ApiModelProperty(value = "订单更新时间戳")
    private Long updatedAt;
}