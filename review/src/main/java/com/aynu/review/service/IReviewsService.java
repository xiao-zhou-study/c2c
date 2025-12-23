package com.aynu.review.service;

import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.review.domain.dto.ReviewCreateDTO;
import com.aynu.review.domain.dto.ReviewReportDTO;
import com.aynu.review.domain.dto.ReviewUpdateDTO;
import com.aynu.review.domain.po.Reviews;
import com.aynu.review.domain.vo.ReviewVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 评价信息表，存储用户对租赁物品/交易的评价（逻辑外键关联物品/用户/订单表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
public interface IReviewsService extends IService<Reviews> {

    /**
     * 创建评价
     * @param createDTO 创建评价DTO
     * @return 评价ID
     */
    Long createReview(ReviewCreateDTO createDTO);

    /**
     * 获取物品评价列表
     * @param itemId 物品ID
     * @param query 分页查询条件
     * @return 评价列表
     */
    PageDTO<ReviewVO> getItemReviews(Long itemId, PageQuery query);

    /**
     * 获取用户收到的评价列表
     * @param userId 用户ID
     * @param query 分页查询条件
     * @return 评价列表
     */
    PageDTO<ReviewVO> getUserReviews(Long userId, PageQuery query);

    /**
     * 获取我发出的评价列表
     * @param query 分页查询条件
     * @return 评价列表
     */
    PageDTO<ReviewVO> getMyReviews(PageQuery query);

    /**
     * 更新评价
     * @param reviewId 评价ID
     * @param updateDTO 更新评价DTO
     * @return 评价详情
     */
    ReviewVO updateReview(Long reviewId, ReviewUpdateDTO updateDTO);

    /**
     * 删除评价
     * @param reviewId 评价ID
     */
    void deleteReview(Long reviewId);

    /**
     * 检查是否可以评价
     * @param orderId 订单ID
     * @return 是否可以评价
     */
    Map<String, Object> canReview(Long orderId);

    /**
     * 举报评价
     * @param reviewId 评价ID
     * @param reportDTO 举报信息
     * @return 是否成功
     */
    Boolean reportReview(Long reviewId, ReviewReportDTO reportDTO);

    /**
     * 获取评价统计
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getReviewStats(Long userId);
}
