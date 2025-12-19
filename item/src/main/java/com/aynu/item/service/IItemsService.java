package com.aynu.item.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.item.domain.dto.ItemsDTO;
import com.aynu.item.domain.po.Items;
import com.aynu.item.domain.vo.ItemsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface IItemsService extends IService<Items> {

    void add(ItemsDTO itemsDTO);

    PageDTO<ItemsVO> listByCategory(String keyword,
                                    Long categoryId,
                                    Long status,
                                    BigDecimal minPrice,
                                    BigDecimal maxPrice,
                                    String conditionLevel,
                                    Boolean isDeposit,
                                    String location,
                                    PageQuery query);
}
