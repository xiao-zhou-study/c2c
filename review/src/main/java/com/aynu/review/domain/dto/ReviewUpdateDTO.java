package com.aynu.review.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.util.List;

@Data
@ApiModel(description = "更新评价DTO")
public class ReviewUpdateDTO {

    @ApiModelProperty(value = "评分（1-5分）")
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
