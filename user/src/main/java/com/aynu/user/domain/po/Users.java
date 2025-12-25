package com.aynu.user.domain.po;

import cn.hutool.core.bean.BeanUtil;
import com.aynu.api.dto.user.UserDTO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 用户基本信息表，存储用户核心登录和基础信息
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("users")
public class Users implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户邮箱，唯一，用于登录/找回密码
     */
    private String email;

    /**
     * 手机号码，唯一，用户登录/找回密码
     */
    private String phone;

    /**
     * 密码哈希值（不可逆加密）
     */
    private String passwordHash;

    /**
     * 头像图片URL地址
     */
    private String avatarUrl;

    /**
     * 学号，唯一，用于登录/找回密码
     */
    private String studentId;

    /**
     * 所属学校
     */
    private String school;

    /**
     * 所属院系
     */
    private String department;

    /**
     * 年级（如2023级）
     */
    private String grade;

    /**
     * 信用分数，初始100分
     */
    private Integer creditScore;

    /**
     * 是否实名认证：FALSE-未认证，TRUE-已认证
     */
    private Boolean isVerified;

    /**
     * 账号状态：1-正常 2-禁用
     */
    private Integer status;

    /**
     * 最后登录时间戳（毫秒级）
     */
    private Long lastLoginAt;

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

    public UserDTO toDTO() {
        UserDTO userDTO = BeanUtil.toBean(this, UserDTO.class);
        userDTO.setPassword(this.getPasswordHash());
        return userDTO;

    }


}
