package com.aynu.api.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemsVO implements Serializable {
    /**
     * 主键ID，自增
     */
    private Long id;

    /**
     * 物品所有者ID（逻辑外键，关联users表id）
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 物品标题（如“九成新笔记本电脑”）
     */
    private String title;

    /**
     * 物品详细描述
     */
    private String description;

    /**
     * 物品分类ID（逻辑外键，关联categories表id）
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 物品成色：new-全新、90%-九成新、80%-八成新等
     */
    private String conditionLevel;

    /**
     * 物品图片URL集合，JSON格式存储
     */
    private String images;

    /**
     * 租赁单价（元）
     */
    private BigDecimal price;

    /**
     * 计费类型：per_day-按天、per_week-按周、per_month-按月
     */
    private String billingType;

    /**
     * 押金金额（元）
     */
    private BigDecimal deposit;

    /**
     * 价格是否可议：FALSE-不可议，TRUE-可议
     */
    private Boolean isNegotiable;

    /**
     * 最小租赁天数
     */
    private Integer minBorrowDays;

    /**
     * 最大租赁天数
     */
    private Integer maxBorrowDays;

    /**
     * 物品所在位置（如“XX校区教学楼”）
     */
    private String location;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 借用条件（如“仅限本校学生”）
     */
    private String borrowConditions;

    /**
     * 物品状态：1-可借用 2-已借出 3-已下架
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 累计被租赁次数
     */
    private Integer borrowCount;

    /**
     * 累计被评价次数
     */
    private Integer totalRatings;

    /**
     * 平均评分（保留2位小数）
     */
    private BigDecimal averageRating;

}
