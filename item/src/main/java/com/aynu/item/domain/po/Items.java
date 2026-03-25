package com.aynu.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("items")
public class Items implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物品所有者 ID（关联 users 表 id）
     */
    private Long ownerId;

    /**
     * 物品标题
     */
    private String title;

    /**
     * 物品详细描述
     */
    private String description;

    /**
     * 物品分类 ID（关联 categories 表 id）
     */
    private Long categoryId;

    /**
     * 物品成色：0-全新、1-九成新、2-八成新等
     * 对应枚举 ConditionLevel
     */
    private Integer conditionLevel;

    /**
     * 物品图片 URL 集合，JSON 格式存储
     */
    private String images;

    /**
     * 售价（元）
     */
    private BigDecimal price;

    /**
     * 物品所在位置
     */
    private String location;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 物品状态：1-待售 2-已售出 3-已下架
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
     * 记录创建时间戳
     */
    private Long createdAt;

    /**
     * 记录更新时间戳
     */
    private Long updatedAt;

}
