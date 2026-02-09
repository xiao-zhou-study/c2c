package com.aynu.campus.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriesCreateDTO implements Serializable {

    /**
     * 分类唯一ID 当id不为空时为修改
     */
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
}
