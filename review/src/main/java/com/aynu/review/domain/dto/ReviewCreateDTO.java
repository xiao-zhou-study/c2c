package com.aynu.review.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(description = "创建评价DTO")
public class ReviewCreateDTO {

    @ApiModelProperty(value = "物品ID", required = true)
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @ApiModelProperty(value = "被评价人ID", required = true)
    @NotNull(message = "被评价人ID不能为空")
    private Long targetUserId;

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "评分（1-5分）", required = true)
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer rating;

    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "评价配图URL列表")
    private List<String> images;

    @ApiModelProperty(value = "是否匿名评价")
    private Boolean isAnonymous;
}
