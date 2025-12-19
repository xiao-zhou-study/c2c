package com.aynu.item.service;

import com.aynu.item.domain.po.ItemStats;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 物品统计表，存储物品行为相关的统计数据（逻辑外键关联items表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface IItemStatsService extends IService<ItemStats> {

}
