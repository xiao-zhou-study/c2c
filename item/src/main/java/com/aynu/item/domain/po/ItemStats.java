package com.aynu.item.domain.po;

import java.io.Serial;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 物品统计表，存储物品行为相关的统计数据（逻辑外键关联items表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("item_stats")
public class ItemStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的物品ID（逻辑外键，关联items表id）
     */
    private Long itemId;

    /**
     * 累计浏览次数
     */
    private Integer viewCount;

    /**
     * 累计收藏次数
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

    /**
     * 记录创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 记录更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
