package com.aynu.campus.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 话题分类表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("categories")
@Schema(description = "话题分类表对象")
public class CategoriesPO implements Serializable {
    @Serial
    private static final long serialVersionUID = 110309269791023488L;

    /**
     * 分类唯一ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类唯一ID")
    private Long id;

    /**
     * 分类名称（如：学习交流、失物招领、二手交易）
     */
    @Schema(description = "分类名称（如：学习交流、失物招领、二手交易）")
    private String name;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述")
    private String description;

    /**
     * 排序权重，数值越大越靠前
     */
    @Schema(description = "排序权重，数值越大越靠前")
    private Integer sortOrder;

    /**
     * 创建时间戳(毫秒)
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间戳(毫秒)")
    private Long createTime;

}
