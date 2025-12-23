package com.aynu.review.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 评价信息表，存储用户对租赁物品/交易的评价（逻辑外键关联物品/用户/订单表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("reviews")
public class Reviews implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联物品ID（逻辑外键，关联items表id）
     */
    private Long itemId;

    /**
     * 评价人ID（逻辑外键，关联users表id）
     */
    private Long reviewerId;

    /**
     * 被评价人ID（逻辑外键，关联users表id）
     */
    private Long targetUserId;

    /**
     * 关联订单ID（逻辑外键，关联borrow_orders表id）
     */
    private Long orderId;

    /**
     * 评分：1-5分（1星最低，5星最高）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价配图URL集合，JSON格式存储
     */
    private String images;

    /**
     * 评价状态：1-正常 2-已删除（软删除）
     */
    private Integer status;

    /**
     * 是否匿名评价：FALSE-不匿名，TRUE-匿名
     */
    private Boolean isAnonymous;

    /**
     * 评价创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 评价更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
