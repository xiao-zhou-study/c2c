package com.aynu.auth.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 认证令牌表，存储用户的访问令牌和刷新令牌信息（时间字段为毫秒级时间戳）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auth_tokens")
public class AuthTokens implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID（逻辑外键）
     */
    private Long userId;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌，用于获取新的访问令牌
     */
    private String refreshToken;

    /**
     * 访问令牌过期时间戳（毫秒级）
     */
    private Long expiresAt;

    /**
     * 刷新令牌过期时间戳（毫秒级）
     */
    private Long refreshExpiresAt;

    /**
     * 令牌类型（如Bearer）
     */
    private String tokenType;

    /**
     * 令牌权限范围，多个权限用逗号分隔
     */
    private String scopes;

    /**
     * 记录创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 记录更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
