package com.aynu.item.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.item.ItemsVO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.api.enums.item.ConditionLevel;
import com.aynu.api.enums.item.ItemStatus;
import com.aynu.api.enums.user.StatsEnum;
import com.aynu.common.autoconfigure.mq.RabbitMqHelper;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.dto.UpdateStatsDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.StringUtils;
import com.aynu.common.utils.UserContext;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.dto.PieChartCountDTO;
import com.aynu.item.domain.po.Categories;
import com.aynu.item.domain.po.Items;
import com.aynu.item.domain.vo.PieChartVO;
import com.aynu.item.mapper.ItemsMapper;
import com.aynu.item.service.ICategoriesService;
import com.aynu.item.service.IItemsService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.aynu.common.constants.MqConstants.Exchange.USER_EXCHANGE;
import static com.aynu.common.constants.MqConstants.Key.USER_UPDATE_STATS;

/**
 * <p>
 * 物品信息表 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemsServiceImpl extends ServiceImpl<ItemsMapper, Items> implements IItemsService {

    private final UserClient userClient;
    private final ICategoriesService categoriesService;
    private final RabbitMqHelper rabbitMqHelper;
    private final ItemsMapper itemsMapper;

    @Override
    public Long add(ItemsDTO itemsDTO) {
        Long userId = UserContext.getUser();
        Items item = new Items();

        this.copyDtoToEntity(itemsDTO, item);

        item.setOwnerId(userId);
        item.setStatus(ItemStatus.FOR_SALE.getValue());
        item.setViewCount(0);
        item.setFavoriteCount(0);
        item.setCreatedAt(System.currentTimeMillis());
        item.setUpdatedAt(System.currentTimeMillis());

        save(item);

        // 发送消息更新用户发布物品数量
        rabbitMqHelper.send(USER_EXCHANGE,
                USER_UPDATE_STATS,
                UpdateStatsDTO.builder()
                        .userId(userId)
                        .type(StatsEnum.ITEMS_PUBLISHED.getValue())
                        .isAdd(true)
                        .build());

        return item.getId();
    }

    @Override
    public PageDTO<ItemsVO> listByCategory(String keyword,
                                           Long categoryId,
                                           Long status,
                                           BigDecimal minPrice,
                                           BigDecimal maxPrice,
                                           Integer conditionLevel,
                                           Boolean isDeposit,
                                           String location,
                                           PageQuery query) {
        LambdaQueryChainWrapper<Items> wrapper = lambdaQuery();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Items::getTitle, keyword)
                    .or()
                    .like(Items::getDescription, keyword));
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
        if (conditionLevel != null) {
            ConditionLevel conditionLevel1 = ConditionLevel.of(conditionLevel);
            wrapper.eq(Items::getConditionLevel, conditionLevel1);
        }
        if (StringUtils.isNotBlank(location)) {
            wrapper.like(Items::getLocation, location);
        }

        Page<Items> page = wrapper.page(query.toMpPage("created_at", false));
        if (CollUtil.isEmpty(page.getRecords())) {
            return PageDTO.empty(page);
        }

        return PageDTO.of(page, convertToVOList(page.getRecords()));
    }

    @Override
    @Transactional
    public boolean update(Long id, ItemsDTO itemsDTO) {
        Items item = getById(id);
        if (item == null) {
            return false;
        }
        this.copyDtoToEntity(itemsDTO, item);
        item.setUpdatedAt(System.currentTimeMillis());
        return updateById(item);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting item with id: {}", id);

        Long userId = UserContext.getUser();

        rabbitMqHelper.send(USER_EXCHANGE,
                USER_UPDATE_STATS,
                UpdateStatsDTO.builder()
                        .userId(userId)
                        .type(StatsEnum.ITEMS_PUBLISHED.getValue())
                        .isAdd(false)
                        .build());

        return lambdaUpdate().eq(Items::getId, id)
                .remove();
    }

    @Override
    public ItemsVO getByIdWithDetail(Long id) {
        Items item = getById(id);
        if (item == null) {
            throw new BadRequestException("物品不存在");
        }

        Map<Long, UserDTO> userMap = getUserMap(Set.of(item.getOwnerId()));
        Map<Long, Categories> categoryMap = getCategoryMap(Set.of(item.getCategoryId()));

        return convertToVO(item, userMap, categoryMap);
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status, String remark) {
        ItemStatus itemStatus = ItemStatus.of(status);
        if (itemStatus == null) {
            throw new BadRequestException("无效的状态值");
        }
        return lambdaUpdate().eq(Items::getId, id)
                .set(Items::getStatus, itemStatus)
                .set(Items::getUpdatedAt, System.currentTimeMillis())
                .update();
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        ItemStatus itemStatus = ItemStatus.of(status);
        if (itemStatus == null) {
            throw new BadRequestException("无效的状态值");
        }
        return lambdaUpdate().in(Items::getId, ids)
                .set(Items::getStatus, itemStatus)
                .set(Items::getUpdatedAt, System.currentTimeMillis())
                .update();
    }

    @Override
    public List<ItemsVO> getByUserId(Long userId) {
        List<Items> items = lambdaQuery().eq(Items::getOwnerId, userId)
                .orderByDesc(Items::getCreatedAt)
                .list();
        return convertToVOList(items);
    }

    @Override
    public Object getStats() {
        List<Items> allItems = lambdaQuery().select(Items::getStatus, Items::getCategoryId)
                .list();
        long totalCount = allItems.size();

        Map<Integer, Long> statusCount = allItems.stream()
                .collect(Collectors.groupingBy(Items::getStatus, Collectors.counting()));

        Map<Long, Long> categoryCount = allItems.stream()
                .map(Items::getCategoryId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return Map.of("totalCount", totalCount, "statusCount", statusCount, "categoryCount", categoryCount);
    }

    @Override
    public List<ItemsVO> getRecommendedItems(Integer limit) {
        List<Items> items = lambdaQuery().eq(Items::getStatus, ItemStatus.FOR_SALE)
                .orderByDesc(Items::getFavoriteCount)
                .last("LIMIT " + limit)
                .list();
        return convertToVOList(items);
    }

    @Override
    public List<ItemsVO> getHotItems(Integer days, Integer limit) {
        long startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);
        List<Items> items = lambdaQuery().eq(Items::getStatus, ItemStatus.FOR_SALE)
                .ge(Items::getCreatedAt, startTime)
                .orderByDesc(Items::getViewCount)
                .last("LIMIT " + limit)
                .list();
        return convertToVOList(items);
    }

    @Override
    public List<ItemsVO> queryByIds(List<Long> ids) {
        List<Items> items = listByIds(ids);

        if (CollUtil.isEmpty(items)) {
            return List.of();
        }

        Set<Long> userIds = items.stream()
                .map(Items::getOwnerId)
                .collect(Collectors.toSet());

        Map<Long, UserDTO> userMap = getUserMap(userIds);

        Set<Long> categoryIds = items.stream()
                .map(Items::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, Categories> categoryMap = getCategoryMap(categoryIds);

        return items.stream()
                .map(item -> convertToVO(item, userMap, categoryMap))
                .collect(Collectors.toList());

    }

    @Override
    public List<PieChartVO> getPieChart() {
        List<PieChartCountDTO> pieChartVOs = itemsMapper.getItemByCategoryCount();

        // 获取物品总数量
        Long count = lambdaQuery().count();

        return pieChartVOs.stream()
                .map(dto -> {
                    PieChartVO pieChartVO = new PieChartVO();
                    pieChartVO.setName(dto.getName());

                    BigDecimal value = BigDecimal.valueOf(dto.getValue());
                    BigDecimal total = BigDecimal.valueOf(count);
                    pieChartVO.setValue(value.divide(total, 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)));
                    return pieChartVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 手动将 DTO 属性映射到 PO 实体（避免反射）
     */
    private void copyDtoToEntity(ItemsDTO dto, Items entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategoryId(dto.getCategoryId());

        entity.setConditionLevel(ConditionLevel.of(dto.getConditionLevel())
                .getValue());

        if (CollUtil.isNotEmpty(dto.getImages())) {
            entity.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }

        entity.setPrice(dto.getPrice());
        entity.setLocation(dto.getLocation());
        entity.setAddress(dto.getAddress());
    }

    private List<ItemsVO> convertToVOList(List<Items> items) {
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }

        Set<Long> userIds = items.stream()
                .map(Items::getOwnerId)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = items.stream()
                .map(Items::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, UserDTO> userMap = getUserMap(userIds);
        Map<Long, Categories> categoryMap = getCategoryMap(categoryIds);

        return items.stream()
                .map(item -> convertToVO(item, userMap, categoryMap))
                .collect(Collectors.toList());
    }

    private ItemsVO convertToVO(Items item, Map<Long, UserDTO> userMap, Map<Long, Categories> categoryMap) {
        ItemsVO vo = new ItemsVO();

        // 显式赋值字段
        vo.setId(item.getId());
        vo.setOwnerId(item.getOwnerId());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setCategoryId(item.getCategoryId());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setPrice(item.getPrice());
        vo.setLocation(item.getLocation());
        vo.setAddress(item.getAddress());
        vo.setStatus(item.getStatus());
        vo.setViewCount(item.getViewCount());
        vo.setFavoriteCount(item.getFavoriteCount());

        if (StringUtils.isNotBlank(item.getImages())) {
            vo.setImages(JSONUtil.toList(item.getImages(), String.class));
        }

        // 填充关联用户信息
        UserDTO userDTO = userMap.get(item.getOwnerId());
        if (userDTO != null) {
            vo.setOwnerName(userDTO.getUsername());
            vo.setOwnerAvatar(userDTO.getAvatarUrl());
        }

        // 填充关联分类信息
        Categories category = categoryMap.get(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }

    private Map<Long, UserDTO> getUserMap(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Map.of();
        }
        try {
            List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
            if (CollUtil.isEmpty(userDTOS)) {
                return Map.of();
            }
            return userDTOS.stream()
                    .collect(Collectors.toMap(UserDTO::getId, Function.identity(), (k1, k2) -> k1));
        } catch (Exception e) {
            log.error("远程调用查询用户信息失败", e);
            return Map.of();
        }
    }

    private Map<Long, Categories> getCategoryMap(Set<Long> categoryIds) {
        Set<Long> validIds = categoryIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(validIds)) {
            return Map.of();
        }
        return categoriesService.lambdaQuery()
                .in(Categories::getId, validIds)
                .list()
                .stream()
                .collect(Collectors.toMap(Categories::getId, Function.identity(), (k1, k2) -> k1));
    }
}