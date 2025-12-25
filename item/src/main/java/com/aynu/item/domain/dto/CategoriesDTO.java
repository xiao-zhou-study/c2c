package com.aynu.item.domain.dto;

import com.aynu.item.domain.po.Categories;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("物品分类DTO")
public class CategoriesDTO implements Serializable {

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

    @ApiModelProperty("排序顺序")
    private Integer sortOrder;

    @ApiModelProperty("是否启用")
    private Boolean isActive;

    public Categories toPO() {
        Categories categories = new Categories();
        categories.setName(name);
        categories.setDescription(description);
        categories.setIcon(icon);
        categories.setSortOrder(sortOrder);
        categories.setIsActive(isActive);
        return categories;
    }
}
