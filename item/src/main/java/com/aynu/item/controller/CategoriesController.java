package com.aynu.item.controller;


import com.aynu.item.domain.dto.CategoriesDTO;
import com.aynu.item.domain.po.Categories;
import com.aynu.item.domain.vo.CategoriesVO;
import com.aynu.item.service.ICategoriesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 物品分类表，定义物品的分类维度 前端控制器
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Api(tags = "物品分类相关接口")
public class CategoriesController {

    private final ICategoriesService categoriesService;

    @ApiOperation("获取所有物品分类")
    @GetMapping
    public List<CategoriesVO> listAll() {
        return categoriesService.listAll();
    }

    @ApiOperation("新增物品分类")
    @PostMapping
    public void add(@RequestBody CategoriesDTO categoriesDTO) {
        Categories categories = categoriesDTO.toPO();
        categories.setSortOrder(100);
        categories.setIsActive(false);
        categories.setCreatedAt(System.currentTimeMillis());
        categories.setUpdatedAt(System.currentTimeMillis());
        categoriesService.save(categories);
    }

    @ApiOperation("删除物品分类")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoriesService.delete(id);
    }

    @ApiOperation("更新物品分类")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody CategoriesDTO categoriesDTO) {
        Categories categories = categoriesDTO.toPO();
        categories.setId(id);
        categories.setUpdatedAt(System.currentTimeMillis());
        categoriesService.updateById(categories);
    }
}
