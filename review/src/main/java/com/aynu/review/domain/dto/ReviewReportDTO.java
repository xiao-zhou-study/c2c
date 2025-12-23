package com.aynu.review.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "举报评价DTO")
public class ReviewReportDTO {

    @ApiModelProperty(value = "举报原因", required = true)
    @NotBlank(message = "举报原因不能为空")
    private String reason;

    @ApiModelProperty(value = "举报说明")
    @Size(max = 500, message = "举报说明长度不能超过500个字符")
    private String description;
}
