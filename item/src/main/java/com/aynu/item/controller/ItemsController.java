package com.aynu.item.controller;

import com.aynu.api.dto.item.ItemsVO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.UserContext;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.service.IItemsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表） 前端控制器
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Api(tags = "物品信息相关接口")
public class ItemsController {

    private final IItemsService itemsService;

    @ApiOperation("新增物品信息")
    @PostMapping
    public Long add(@Valid @RequestBody ItemsDTO itemsDTO) {
        return itemsService.add(itemsDTO);
    }

    @ApiOperation("更新物品信息")
    @PutMapping("/{id}")
    public Boolean update(@PathVariable Long id, @Valid @RequestBody ItemsDTO itemsDTO) {
        return itemsService.update(id, itemsDTO);
    }

    @ApiOperation("删除物品信息")
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return itemsService.delete(id);
    }

    @ApiOperation("根据ID获取物品详情")
    @GetMapping("/{id}")
    public ItemsVO getById(@PathVariable Long id) {
        return itemsService.getByIdWithDetail(id);
    }

    @ApiOperation("批量更新物品状态")
    @PutMapping("/batch/status")
    public Boolean batchUpdateStatus(@RequestParam List<Long> ids,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String remark) {
        return itemsService.batchUpdateStatus(ids, status, remark);
    }

    @ApiOperation("根据分类分页查询物品")
    @GetMapping
    public PageDTO<ItemsVO> listByCategory(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Long status,
                                           @RequestParam(required = false) BigDecimal minPrice,
                                           @RequestParam(required = false) BigDecimal maxPrice,
                                           @RequestParam(required = false) String conditionLevel,
                                           @RequestParam(required = false) Boolean isDeposit,
                                           @RequestParam(required = false) String location,
                                           PageQuery query) {
        return itemsService.listByCategory(keyword,
                categoryId,
                status,
                minPrice,
                maxPrice,
                conditionLevel,
                isDeposit,
                location,
                query);
    }

    @ApiOperation("根据用户ID获取物品列表")
    @GetMapping("/user/{userId}")
    public List<ItemsVO> getByUserId(@PathVariable Long userId) {
        return itemsService.getByUserId(userId);
    }

    @ApiOperation("获取“我的”发布物品列表")
    @GetMapping("/my")
    public List<ItemsVO> getMyItems() {
        return itemsService.getByUserId(UserContext.getUser());
    }

    @ApiOperation("获取物品统计信息")
    @GetMapping("/stats")
    public Object getStats() {
        return itemsService.getStats();
    }

    @ApiOperation("获取推荐物品")
    @GetMapping("/recommended")
    public List<ItemsVO> getRecommendedItems(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        return itemsService.getRecommendedItems(limit);
    }

    @ApiOperation("获取热门物品")
    @GetMapping("/hot")
    public List<ItemsVO> getHotItems(@RequestParam(required = false, defaultValue = "7") Integer days,
                                     @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return itemsService.getHotItems(days, limit);
    }
}
