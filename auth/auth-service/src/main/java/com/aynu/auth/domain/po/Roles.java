package com.aynu.auth.domain.po;

import com.aynu.api.dto.auth.RoleDTO;
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
 * 权限角色表，定义系统中的角色类型（时间字段为毫秒级时间戳）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("roles")
public class Roles implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称（如admin、user），唯一
     */
    private String name;

    /**
     * 角色描述，说明该角色的权限范围
     */
    private String description;

    /**
     * 记录创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 记录更新时间戳（毫秒级）
     */
    private Long updatedAt;


    /**
     * 转换为RoleDTO对象
     *
     * @return RoleDTO
     */
    public RoleDTO toDTO() {
        RoleDTO dto = new RoleDTO();
        dto.setId(this.id);
        dto.setCode(this.name);
        dto.setName(this.description);
        return dto;
    }

    /**
     * 从RoleDTO构造Roles对象
     *
     * @param dto RoleDTO对象
     */
    public Roles(RoleDTO dto) {
        if (dto != null) {
            this.name = dto.getCode();
            this.description = dto.getName();
        }
    }

    /**
     * 无参构造方法
     */
    public Roles() {
    }
}
