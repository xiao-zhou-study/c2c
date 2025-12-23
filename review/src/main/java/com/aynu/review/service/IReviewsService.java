package com.aynu.review.service;

import com.aynu.review.domain.po.Reviews;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评价信息表，存储用户对租赁物品/交易的评价（逻辑外键关联物品/用户/订单表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface IReviewsService extends IService<Reviews> {

}
