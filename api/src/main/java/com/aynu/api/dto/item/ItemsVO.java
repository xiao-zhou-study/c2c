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
     * 主键 ID，采用雪花算法
     */
    private Long id;

    /**
     * 物品所有者 ID（逻辑外键，关联 users 表 id）
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
     * 物品标题（如"九成新笔记本电脑”）
     */
    private String title;

    /**
     * 物品详细描述
     */
    private String description;

    /**
     * 物品分类 ID（逻辑外键，关联 categories 表 id）
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
     * 物品图片 URL 集合，JSON 格式存储
     */
    private List<String> images;

    /**
     * 售价（元）
     */
    private BigDecimal price;

    /**
     * 物品所在位置（如"XX 校区教学楼”）
     */
    private String location;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 物品状态：1-待售 2-已售出 3-已下架
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
