package com.aynu.item.domain.po;

import com.aynu.item.domain.vo.CategoriesVO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 物品分类表，定义物品的分类维度
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("categories")
public class Categories implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称（如电子产品、书籍），唯一
     */
    private String name;

    /**
     * 分类描述，说明该分类包含的物品类型
     */
    private String description;

    /**
     * 分类图标URL/标识
     */
    private String icon;

    /**
     * 排序序号，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 是否启用：TRUE-启用，FALSE-禁用
     */
    private Boolean isActive;

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


    public CategoriesVO toVO() {
        return CategoriesVO.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .icon(this.icon)
                .sortOrder(this.sortOrder)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
