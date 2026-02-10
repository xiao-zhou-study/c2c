package com.aynu.api.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemsVO implements Serializable {
    /**
     * 主键ID，采用雪花算法
     */
    private Long id;

    /**
     * 物品所有者ID（逻辑外键，关联users表id）
     */
    private Long ownerId;

    /**
     * 物品所有者名称
     */
    private String ownerName;

    /**
     * 物品所有者头像
     */
    private String ownerAvatar;

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
     * 物品分类名称
     */
    private String categoryName;

    /**
     * 物品成色：0-全新、1-九成新、2-八成新等
     * 对应枚举 ConditionLevel
     */
    private Integer conditionLevel;

    /**
     * 物品图片URL集合，JSON格式存储
     */
    private List<String> images;

    /**
     * 租赁单价（元）
     */
    private BigDecimal price;

    /**
     * 计费类型：1-按天、2-按周、3-按月
     * 对应枚举 BillingType
     */
    private Integer billingType;

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
     * 对应枚举 ItemStatus
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
     * 记录创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 记录更新时间戳（毫秒级）
     */
    private Long updatedAt;
}