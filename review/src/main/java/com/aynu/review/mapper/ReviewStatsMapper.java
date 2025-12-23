package com.aynu.review.mapper;

import com.aynu.review.domain.po.ReviewStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 评价统计表，存储用户收到的评价汇总数据（逻辑外键关联users表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface ReviewStatsMapper extends BaseMapper<ReviewStats> {

}
