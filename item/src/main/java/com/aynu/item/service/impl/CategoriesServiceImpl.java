package com.aynu.item.service.impl;

import com.aynu.item.domain.po.Categories;
import com.aynu.item.domain.vo.CategoriesVO;
import com.aynu.item.mapper.CategoriesMapper;
import com.aynu.item.service.ICategoriesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 物品分类表，定义物品的分类维度 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoriesServiceImpl extends ServiceImpl<CategoriesMapper, Categories> implements ICategoriesService {


    @Override
    public List<CategoriesVO> listAll() {
        List<Categories> list = lambdaQuery().eq(Categories::getIsActive, true).list();
        return list.stream().map(Categories::toVO).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }
}
