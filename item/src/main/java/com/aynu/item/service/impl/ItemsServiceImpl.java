package com.aynu.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.StringUtils;
import com.aynu.common.utils.UserContext;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.po.Categories;
import com.aynu.item.domain.po.ItemStats;
import com.aynu.item.domain.po.Items;
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
    public Long add(ItemsDTO itemsDTO) {
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
        return item.getId();
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

    @Override
    @Transactional
    public boolean update(Long id, ItemsDTO itemsDTO) {
        Items item = getById(id);
        if (item == null) {
            return false;
        }

        // 更新物品信息
        BeanUtil.copyProperties(itemsDTO, item);
        item.setUpdatedAt(System.currentTimeMillis());

        return updateById(item);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Items item = getById(id);
        if (item == null) {
            return false;
        }

        // 软删除
        item.setStatus(3); // 设为已下架
        item.setUpdatedAt(System.currentTimeMillis());

        return updateById(item);
    }

    @Override
    public ItemsVO getByIdWithDetail(Long id) {
        Items item = getById(id);
        if (item == null) {
            throw new BadRequestException("物品不存在");
        }

        ItemsVO vo = BeanUtil.toBean(item, ItemsVO.class);

        // 获取用户信息
        UserDTO userDTO = userClient.queryUserByIds(Set.of(item.getOwnerId())).stream().findFirst().orElse(null);
        if (userDTO != null) {
            vo.setUsername(userDTO.getUsername());
            vo.setAvatar(userDTO.getAvatarUrl());
            vo.setUserId(userDTO.getId());
        }

        // 获取分类信息
        Categories category = categoriesService.getById(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return vo;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status, String remark) {
        Items item = getById(id);
        if (item == null) {
            return false;
        }

        item.setStatus(status);
        item.setUpdatedAt(System.currentTimeMillis());

        return updateById(item);
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> ids, Integer status, String remark) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        return lambdaUpdate().in(Items::getId, ids)
                .set(Items::getStatus, status)
                .set(Items::getUpdatedAt, System.currentTimeMillis())
                .update();
    }

    @Override
    public List<ItemsVO> getByUserId(Long userId) {
        List<Items> items = lambdaQuery().eq(Items::getOwnerId, userId).orderByDesc(Items::getCreatedAt).list();

        if (CollUtil.isEmpty(items)) {
            return List.of();
        }

        // 获取分类信息
        Set<Long> categoryIds = items.stream()
                .map(Items::getCategoryId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, Categories> categoryMap = Map.of();
        if (CollUtil.isNotEmpty(categoryIds)) {
            categoryMap = categoriesService.lambdaQuery()
                    .in(Categories::getId, categoryIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(Categories::getId, Function.identity()));
        }

        Map<Long, Categories> finalCategoryMap = categoryMap;
        return items.stream().map(item -> {
            ItemsVO vo = BeanUtil.toBean(item, ItemsVO.class);

            // 设置分类名称
            Categories category = finalCategoryMap.get(item.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            return vo;
        }).toList();
    }

    @Override
    public Object getStats() {
        // 获取物品总数
        long totalCount = count();

        // 按状态统计
        Map<Integer, Long> statusCount = lambdaQuery().select(Items::getStatus)
                .list()
                .stream()
                .collect(Collectors.groupingBy(Items::getStatus, Collectors.counting()));

        // 按分类统计
        Map<Long, Long> categoryCount = lambdaQuery().select(Items::getCategoryId)
                .list()
                .stream()
                .filter(item -> item.getCategoryId() != null && item.getCategoryId() > 0)
                .collect(Collectors.groupingBy(Items::getCategoryId, Collectors.counting()));

        return Map.of("totalCount", totalCount, "statusCount", statusCount, "categoryCount", categoryCount);
    }

    @Override
    public List<ItemsVO> getRecommendedItems(Integer limit) {
        // 推荐逻辑：获取收藏数最多的可借用物品
        List<Items> items = lambdaQuery()
                .eq(Items::getStatus, 1) // 只推荐可借用的物品
                .orderByDesc(Items::getFavoriteCount)
                .last("LIMIT " + limit)
                .list();

        return convertToVOList(items);
    }

    @Override
    public List<ItemsVO> getHotItems(Integer days, Integer limit) {
        // 热门逻辑：获取指定天数内浏览数最多的物品
        long startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);

        List<Items> items = lambdaQuery()
                .eq(Items::getStatus, 1) // 只显示可借用的物品
                .ge(Items::getCreatedAt, startTime) // 创建时间在指定天数内
                .orderByDesc(Items::getViewCount)
                .last("LIMIT " + limit)
                .list();

        return convertToVOList(items);
    }

    private List<ItemsVO> convertToVOList(List<Items> items) {
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }

        // 获取用户信息
        Set<Long> userIds = items.stream().map(Items::getOwnerId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        // 获取分类信息
        Set<Long> categoryIds = items.stream()
                .map(Items::getCategoryId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, Categories> categoryMap = Map.of();
        if (CollUtil.isNotEmpty(categoryIds)) {
            categoryMap = categoriesService.lambdaQuery()
                    .in(Categories::getId, categoryIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(Categories::getId, Function.identity()));
        }

        Map<Long, Categories> finalCategoryMap = categoryMap;
        return items.stream().map(item -> {
            ItemsVO vo = BeanUtil.toBean(item, ItemsVO.class);

            // 设置用户信息
            UserDTO userDTO = userMap.get(item.getOwnerId());
            if (userDTO != null) {
                vo.setUsername(userDTO.getUsername());
                vo.setAvatar(userDTO.getAvatarUrl());
                vo.setUserId(userDTO.getId());
            }

            // 设置分类信息
            Categories category = finalCategoryMap.get(item.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            return vo;
        }).toList();
    }
}
