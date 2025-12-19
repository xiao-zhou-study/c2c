package com.aynu.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.StringUtils;
import com.aynu.common.utils.UserContext;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.po.Categories;
import com.aynu.item.domain.po.ItemStats;
import com.aynu.item.domain.po.Items;
import com.aynu.item.domain.vo.ItemsVO;
import com.aynu.item.mapper.ItemsMapper;
import com.aynu.item.service.ICategoriesService;
import com.aynu.item.service.IItemStatsService;
import com.aynu.item.service.IItemsService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemsServiceImpl extends ServiceImpl<ItemsMapper, Items> implements IItemsService {

    private final IItemStatsService itemStatsService;
    private final UserClient userClient;
    private final ICategoriesService categoriesService;


    @Override
    @Transactional
    public void add(ItemsDTO itemsDTO) {
        Long userId = UserContext.getUser();
        Items item = BeanUtil.toBean(itemsDTO, Items.class);
        item.setOwnerId(userId);
        item.setStatus(1);
        item.setViewCount(0);
        item.setFavoriteCount(0);
        item.setCreatedAt(System.currentTimeMillis());
        item.setUpdatedAt(System.currentTimeMillis());
        save(item);
        ItemStats itemStats = new ItemStats();
        itemStats.setItemId(item.getId());
        itemStats.setViewCount(0);
        itemStats.setFavoriteCount(0);
        itemStats.setBorrowCount(0);
        itemStats.setTotalRatings(0);
        itemStats.setAverageRating(BigDecimal.ZERO);
        itemStats.setUpdatedAt(System.currentTimeMillis());
        itemStats.setCreatedAt(System.currentTimeMillis());
        itemStatsService.save(itemStats);
    }

    @Override
    public PageDTO<ItemsVO> listByCategory(String keyword,
                                           Long categoryId,
                                           Long status,
                                           BigDecimal minPrice,
                                           BigDecimal maxPrice,
                                           String conditionLevel,
                                           Boolean isDeposit,
                                           String location,
                                           PageQuery query) {
        LambdaQueryChainWrapper<Items> wrapper = lambdaQuery();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Items::getTitle, keyword).or().like(Items::getDescription, keyword));
        }

        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Items::getCategoryId, categoryId);
        }

        if (status != null && status > 0) {
            wrapper.eq(Items::getStatus, status);
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
            wrapper.ge(Items::getPrice, minPrice);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            wrapper.le(Items::getPrice, maxPrice);
        }

        if (StringUtils.isNotBlank(conditionLevel)) {
            wrapper.eq(Items::getConditionLevel, conditionLevel);
        }

        if (isDeposit != null && isDeposit) {
            wrapper.eq(Items::getDeposit, BigDecimal.ZERO);
        }

        if (StringUtils.isNotBlank(location)) {
            wrapper.like(Items::getLocation, location);
        }

        Page<Items> page = wrapper.page(query.toMpPage("create_at", false));
        List<Items> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        Set<Long> userIds = records.stream().map(Items::getOwnerId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        Categories categories = categoriesService.lambdaQuery().eq(Categories::getId, categoryId).one();

        List<ItemsVO> list = records.stream().map(item -> {
            ItemsVO vo = BeanUtil.toBean(item, ItemsVO.class);

            UserDTO userDTO = userMap.get(item.getOwnerId());
            if (userDTO != null) {
                vo.setUsername(userDTO.getUsername());
                vo.setAvatar(userDTO.getAvatarUrl());
                vo.setUserId(userDTO.getId());
            }
            vo.setCategoryName(categories.getName());
            return vo;

        }).toList();

        return PageDTO.of(page, list);
    }
}
