package com.aynu.auth.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 用户角色关联表，实现用户与角色的多对多关联（时间字段为毫秒级时间戳，使用逻辑外键）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_roles")
public class UserRoles implements Serializable {

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
     * 关联的角色ID（逻辑外键，关联roles表id）
     */
    private Long roleId;

    /**
     * 记录创建时间戳（毫秒级）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;


}
