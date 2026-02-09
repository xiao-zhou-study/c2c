package com.aynu.campus.controller;


import com.aynu.campus.domain.dto.CategoriesCreateDTO;
import com.aynu.campus.domain.po.CategoriesPO;
import com.aynu.campus.service.CategoriesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "话题分类表接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;

    /**
     * 新增或修改话题分类
     *
     * @param dto 话题分类对象
     */
    @PostMapping("/add")
    public void addCategory(@RequestBody CategoriesCreateDTO dto) {
        categoriesService.addCategory(dto);
    }

    /**
     * 删除话题分类
     *
     * @param id 分类Id
     */
    @DeleteMapping("/delete")
    public void deleteCategory(@RequestParam Long id) {
        categoriesService.deleteCategory(id);
    }

    /**
     * 获取分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/list")
    public List<CategoriesPO> getCategoriesList() {
        return categoriesService.getCategoriesList();
    }


}
