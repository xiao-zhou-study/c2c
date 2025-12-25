package com.aynu.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("物品分类VO")
public class CategoriesVO implements Serializable {

    /**
     * 主键ID，自增
     */
    @ApiModelProperty("主键ID，自增")
    private Long id;

    /**
     * 分类名称（如电子产品、书籍），唯一
     */
    @ApiModelProperty("分类名称（如电子产品、书籍），唯一")
    private String name;

    /**
     * 分类描述，说明该分类包含的物品类型
     */
    @ApiModelProperty("分类描述，说明该分类包含的物品类型")
    private String description;

    /**
     * 分类图标URL/标识
     */
    @ApiModelProperty("分类图标URL/标识")
    private String icon;

    /**
     * 排序序号，数值越小越靠前
     */
    @ApiModelProperty("排序序号，数值越小越靠前")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @ApiModelProperty("是否启用")
    private Boolean isActive;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Long createdAt;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Long updatedAt;
}
