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

    @ApiModelProperty("物品 ID（更新时必填）")
    private Long id;

    @ApiModelProperty("物品所有者 ID")
    private Long ownerId;

    @ApiModelProperty("物品标题")
    @NotBlank(message = "物品标题不能为空")
    @Size(max = 100, message = "标题不能超过 100 字")
    private String title;

    @ApiModelProperty("物品详细描述")
    @NotBlank(message = "描述不能为空")
    private String description;

    @ApiModelProperty("分类 ID")
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @ApiModelProperty("物品成色：0-全新、1-九成新等")
    @NotNull(message = "成色不能为空")
    private Integer conditionLevel;

    @ApiModelProperty("图片列表")
    @NotEmpty(message = "请至少上传一张图片")
    private List<String> images;

    @ApiModelProperty("售价")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    @ApiModelProperty("物品状态：1-待售 2-已售出 3-已下架")
    private Integer status;

    @ApiModelProperty("校区/区域位置")
    @NotBlank(message = "位置不能为空")
    private String location;

    @ApiModelProperty("详细地址")
    private String address;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("收藏次数")
    private Integer favoriteCount;

    @ApiModelProperty("创建时间")
    private Long createdAt;

    @ApiModelProperty("更新时间")
    private Long updatedAt;
}
