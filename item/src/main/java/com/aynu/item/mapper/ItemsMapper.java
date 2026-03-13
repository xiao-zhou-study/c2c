package com.aynu.item.mapper;

import com.aynu.item.domain.dto.PieChartCountDTO;
import com.aynu.item.domain.po.Items;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 物品信息表，存储租赁物品的核心信息（逻辑外键关联用户/分类表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface ItemsMapper extends BaseMapper<Items> {

    List<PieChartCountDTO> getItemByCategoryCount();

}
