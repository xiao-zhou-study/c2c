package com.aynu.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表）")
public class ItemsDTO implements Serializable {

    /**
     * 物品标题（如“九成新笔记本电脑”）
     */
    @ApiModelProperty("物品标题（如“九成新笔记本电脑”）")
    @NotNull(message = "物品标题不能为空")
    private String title;

    /**
     * 物品详细描述
     */
    @NotNull(message = "物品详细描述不能为空")
    @ApiModelProperty("物品详细描述")
    private String description;

    /**
     * 物品分类ID（逻辑外键，关联categories表id）
     */
    @NotNull(message = "物品分类ID不能为空")
    @ApiModelProperty("物品分类ID（逻辑外键，关联categories表id）")
    private Long categoryId;

    /**
     * 物品成色：new-全新、90%-九成新、80%-八成新等
     */
    @NotNull(message = "物品成色不能为空")
    @ApiModelProperty("物品成色：new-全新、90%-九成新、80%-八成新等")
    private String conditionLevel;

    /**
     * 物品图片URL集合，JSON格式存储
     */
    @NotNull(message = "物品图片URL不能为空")
    @ApiModelProperty("物品图片URL集合，JSON格式存储")
    private List<String> images;

    /**
     * 租赁单价（元）
     */
    @NotNull(message = "物品价格不能为空")
    @ApiModelProperty("租赁单价（元）")
    private BigDecimal price;

    /**
     * 计费类型：per_day-按天、per_week-按周、per_month-按月
     */
    @NotNull(message = "计费类型不能为空")
    @ApiModelProperty("计费类型：per_day-按天、per_week-按周、per_month-按月")
    private String billingType;

    /**
     * 押金金额（元）
     */
    @NotNull(message = "押金金额不能为空")
    @ApiModelProperty("押金金额（元）")
    private BigDecimal deposit;

    /**
     * 价格是否可议：FALSE-不可议，TRUE-可议
     */
    @NotNull(message = "价格是否可议不能为空")
    @ApiModelProperty("价格是否可议：FALSE-不可议，TRUE-可议")
    private Boolean isNegotiable;

    /**
     * 最小租赁天数
     */
    @NotNull(message = "最小租赁天数不能为空")
    @ApiModelProperty("最小租赁天数")
    private Integer minBorrowDays;

    /**
     * 最大租赁天数
     */
    @NotNull(message = "最大租赁天数不能为空")
    @ApiModelProperty("最大租赁天数")
    private Integer maxBorrowDays;

    /**
     * 物品所在位置（如“XX校区教学楼”）
     */
    @NotNull(message = "物品所在位置不能为空")
    @ApiModelProperty("物品所在位置（如“XX校区教学楼”）")
    private String location;

    /**
     * 详细地址
     */
    @NotNull(message = "详细地址不能为空")
    @ApiModelProperty("详细地址")
    private String address;

    /**
     * 借用条件（如“仅限本校学生”）
     */
    @NotNull(message = "借用条件不能为空")
    @ApiModelProperty("借用条件（如“仅限本校学生”）")
    private String borrowConditions;
}
