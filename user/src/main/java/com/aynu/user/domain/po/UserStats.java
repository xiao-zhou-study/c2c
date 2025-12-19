package com.aynu.user.domain.po;

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
 * 用户统计信息表，存储用户行为相关的统计数据（逻辑外键关联users表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_stats")
public class UserStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID（逻辑外键，关联users表id）
     */
    private Long userId;

    /**
     * 发布物品数量
     */
    private Integer itemsPublished;

    /**
     * 借用物品数量
     */
    private Integer itemsBorrowed;

    /**
     * 借出物品数量
     */
    private Integer itemsLent;

    /**
     * 累计被评价次数
     */
    private Integer totalRatings;

    /**
     * 平均评分（保留2位小数）
     */
    private BigDecimal averageRating;

    /**
     * 记录创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 记录更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
