package com.aynu.order.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "订单操作DTO")
public class OrderActionDTO {

    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    @ApiModelProperty(value = "取消/拒绝原因")
    private String reason;
}
