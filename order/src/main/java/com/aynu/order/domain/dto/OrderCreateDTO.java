package com.aynu.order.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "创建订单DTO")
public class OrderCreateDTO {

    @ApiModelProperty(value = "物品ID", required = true)
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @ApiModelProperty(value = "借用天数", required = true)
    @NotNull(message = "借用天数不能为空")
    @Min(value = 1, message = "借用天数至少为1天")
    private Integer borrowDays;

    @ApiModelProperty(value = "借用用途说明")
    @Size(max = 500, message = "用途说明长度不能超过500个字符")
    private String purpose;
}
