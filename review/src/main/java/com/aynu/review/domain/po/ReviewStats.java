package com.aynu.review.domain.po;

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
 * 评价统计表，存储用户收到的评价汇总数据（逻辑外键关联users表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("review_stats")
public class ReviewStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID（逻辑外键，关联users表id）
     */
    private Long userId;

    /**
     * 累计收到的评价总数
     */
    private Integer totalReviews;

    /**
     * 平均评分（保留2位小数）
     */
    private BigDecimal avgRating;

    /**
     * 1星评价数量
     */
    private Integer oneStarCount;

    /**
     * 2星评价数量
     */
    private Integer twoStarCount;

    /**
     * 3星评价数量
     */
    private Integer threeStarCount;

    /**
     * 4星评价数量
     */
    private Integer fourStarCount;

    /**
     * 5星评价数量
     */
    private Integer fiveStarCount;

    /**
     * 统计记录创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 统计记录更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
