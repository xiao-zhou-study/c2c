package com.aynu.review.mapper;

import com.aynu.review.domain.po.Reviews;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 评价信息表，存储用户对租赁物品/交易的评价（逻辑外键关联物品/用户/订单表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface ReviewsMapper extends BaseMapper<Reviews> {

}
