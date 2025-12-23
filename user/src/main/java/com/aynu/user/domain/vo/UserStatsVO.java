package com.aynu.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户统计信息VO")
public class UserStatsVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "关联的用户ID")
    private Long userId;

    @ApiModelProperty(value = "发布物品数量")
    private Integer itemsPublished;

    @ApiModelProperty(value = "借用物品数量")
    private Integer itemsBorrowed;

    @ApiModelProperty(value = "借出物品数量")
    private Integer itemsLent;

    @ApiModelProperty(value = "累计被评价次数")
    private Integer totalRatings;

    @ApiModelProperty(value = "平均评分（保留2位小数）")
    private BigDecimal averageRating;

    @ApiModelProperty(value = "记录创建时间戳（毫秒级）")
    private Long createdAt;

    @ApiModelProperty(value = "记录更新时间戳（毫秒级）")
    private Long updatedAt;
}
