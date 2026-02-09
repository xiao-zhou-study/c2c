package com.aynu.campus.service;

import com.aynu.campus.domain.dto.CategoriesCreateDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aynu.campus.domain.po.CategoriesPO;

import java.util.List;

public interface CategoriesService extends IService<CategoriesPO> {

    void addCategory(CategoriesCreateDTO dto);

    void deleteCategory(Long id);

    List<CategoriesPO> getCategoriesList();

}
