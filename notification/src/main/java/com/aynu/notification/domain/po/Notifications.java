package com.aynu.notification.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 通知消息表，存储系统/业务推送的消息（逻辑外键关联用户/物品/订单表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("notifications")
public class Notifications implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息接收人ID（逻辑外键，关联users表id）
     */
    private Long receiverId;

    /**
     * 消息发送人ID（逻辑外键，关联users表id；系统消息为NULL）
     */
    private Long senderId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文内容
     */
    private String content;

    /**
     * 消息类型：system-系统通知、borrow-借用相关、return-归还相关、review-评价相关等
     */
    private String type;

    /**
     * 关联业务ID（逻辑外键，结合related_type使用：如item-物品ID、order-订单ID）
     */
    private Long relatedId;

    /**
     * 关联业务类型：item-物品、order-订单等
     */
    private String relatedType;

    /**
     * 是否已读：FALSE-未读、TRUE-已读
     */
    private Boolean isRead;

    /**
     * 已读时间戳（毫秒级，未读则为NULL）
     */
    private Long readAt;

    /**
     * 是否删除：FALSE-未删除、TRUE-已删除（软删除）
     */
    private Boolean isDeleted;

    /**
     * 消息创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 消息更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
