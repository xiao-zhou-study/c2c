package com.aynu.item.controller;


import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.vo.ItemsVO;
import com.aynu.item.service.IItemsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
    public void add(@RequestBody ItemsDTO itemsDTO) {
        itemsService.add(itemsDTO);
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


}
