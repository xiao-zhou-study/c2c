package com.aynu.review.service.impl;

import com.aynu.review.domain.po.ReviewStats;
import com.aynu.review.mapper.ReviewStatsMapper;
import com.aynu.review.service.IReviewStatsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评价统计表，存储用户收到的评价汇总数据（逻辑外键关联users表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Service
public class ReviewStatsServiceImpl extends ServiceImpl<ReviewStatsMapper, ReviewStats> implements IReviewStatsService {

}
