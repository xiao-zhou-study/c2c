package com.aynu.item.service;

import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.po.Categories;
import com.aynu.item.domain.vo.CategoriesVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 物品分类表，定义物品的分类维度 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface ICategoriesService extends IService<Categories> {

    List<CategoriesVO> listAll();

    void delete(Long id);

}
