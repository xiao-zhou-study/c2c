package com.aynu.review.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "回复评价DTO")
public class ReportDTO {
    @ApiModelProperty(value = "回复内容")
    private String content;
}
