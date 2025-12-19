package com.aynu.item.mapper;

import com.aynu.item.domain.po.ItemStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 物品统计表，存储物品行为相关的统计数据（逻辑外键关联items表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface ItemStatsMapper extends BaseMapper<ItemStats> {

}
