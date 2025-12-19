package com.aynu.item.service.impl;

import com.aynu.item.domain.po.ItemStats;
import com.aynu.item.mapper.ItemStatsMapper;
import com.aynu.item.service.IItemStatsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 物品统计表，存储物品行为相关的统计数据（逻辑外键关联items表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Service
public class ItemStatsServiceImpl extends ServiceImpl<ItemStatsMapper, ItemStats> implements IItemStatsService {

}
