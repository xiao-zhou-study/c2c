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

    @ApiModelProperty(value = "计费类型：per_day-按天、per_week-按周、per_month-按月")
    private String billingType;

    @ApiModelProperty(value = "押金金额")
    private BigDecimal deposit;

    @ApiModelProperty(value = "租赁天数")
    private Integer borrowDays;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "借用用途说明")
    private String purpose;

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
