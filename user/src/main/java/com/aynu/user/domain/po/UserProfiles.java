package com.aynu.user.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 用户详细信息表，存储用户扩展信息（逻辑外键关联users表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_profiles")
public class UserProfiles implements Serializable {

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
     * 真实姓名
     */
    private String realName;

    /**
     * 性别：1-男 2-女 0-保密
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 个人简介/个性签名
     */
    private String bio;

    /**
     * QQ号码
     */
    private String qq;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 记录创建时间戳（毫秒级）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    /**
     * 记录更新时间戳（毫秒级）
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;


}
