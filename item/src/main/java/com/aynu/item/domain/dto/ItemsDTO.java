package com.aynu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("物品保存/更新请求参数")
public class ItemsDTO implements Serializable {

    @ApiModelProperty("物品ID（更新时必填）")
    private Long id; // 新增时为 null，更新时必填

    @ApiModelProperty("物品标题")
    @NotBlank(message = "物品标题不能为空")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @ApiModelProperty("物品详细描述")
    @NotBlank(message = "描述不能为空")
    private String description;

    @ApiModelProperty("分类ID")
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @ApiModelProperty("物品成色：0-全新、1-九成新等")
    @NotNull(message = "成色不能为空")
    private Integer conditionLevel;

    @ApiModelProperty("图片列表")
    @NotEmpty(message = "请至少上传一张图片")
    private List<String> images;

    @ApiModelProperty("租赁单价")
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @ApiModelProperty("计费类型")
    @NotNull(message = "计费类型不能为空")
    private Integer billingType; // 建议改用枚举

    @ApiModelProperty("押金")
    @NotNull(message = "押金不能为空")
    @DecimalMin(value = "0.00", message = "押金不能为负数")
    private BigDecimal deposit;

    @ApiModelProperty("是否可议价")
    @NotNull(message = "是否可议价不能为空")
    private Boolean isNegotiable;

    @ApiModelProperty("最小租赁天数")
    @Min(value = 1, message = "最小租期为1天")
    private Integer minBorrowDays;

    @ApiModelProperty("最大租赁天数")
    private Integer maxBorrowDays;

    @ApiModelProperty("校区/区域位置")
    @NotBlank(message = "位置不能为空")
    private String location;

    @ApiModelProperty("详细地址")
    private String address;

    @ApiModelProperty("借用条件限制")
    private String borrowConditions;
}
