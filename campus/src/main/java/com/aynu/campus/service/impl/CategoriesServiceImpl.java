package com.aynu.campus.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aynu.campus.domain.dto.CategoriesCreateDTO;
import com.aynu.campus.domain.po.CategoriesPO;
import com.aynu.campus.domain.po.TopicsPO;
import com.aynu.campus.mapper.CategoriesMapper;
import com.aynu.campus.mapper.TopicsMapper;
import com.aynu.campus.service.CategoriesService;
import com.aynu.common.exceptions.BadRequestException;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriesServiceImpl extends ServiceImpl<CategoriesMapper, CategoriesPO> implements CategoriesService {

    private final TopicsMapper topicsMapper;

    @Override
    public void addCategory(CategoriesCreateDTO dto) {
        Long id = dto.getId();

        CategoriesPO categories = lambdaQuery().eq(CategoriesPO::getId, id)
                .one();

        if (categories == null) {
            categories = new CategoriesPO();
            categories.setName(dto.getName());
            categories.setDescription(dto.getDescription());
            categories.setSortOrder(dto.getSortOrder());
            categories.setCreateTime(System.currentTimeMillis());
        } else {
            copyToPO(dto, categories);
        }
        saveOrUpdate(categories);
    }

    @Override
    public void deleteCategory(Long id) {
        CategoriesPO one = lambdaQuery().eq(CategoriesPO::getId, id)
                .one();

        if (one == null) {
            throw new BadRequestException("分类不存在");
        }

        LambdaQueryChainWrapper<TopicsPO> wrapper = new LambdaQueryChainWrapper<>(topicsMapper);
        List<TopicsPO> topicsPOS = wrapper.eq(TopicsPO::getCategoryId, id)
                .list();

        if (CollUtil.isNotEmpty(topicsPOS)) {
            throw new BadRequestException("分类下存在话题，不能删除");
        }

        removeById(id);
    }

    @Override
    public List<CategoriesPO> getCategoriesList() {
        return lambdaQuery().orderByAsc(CategoriesPO::getSortOrder)
                .list();
    }

    private void copyToPO(CategoriesCreateDTO dto, CategoriesPO categories) {
        categories.setName(dto.getName());
        categories.setDescription(dto.getDescription());
        categories.setSortOrder(dto.getSortOrder());
    }
}
