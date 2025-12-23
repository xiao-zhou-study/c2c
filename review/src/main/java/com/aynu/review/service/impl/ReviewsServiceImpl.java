package com.aynu.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.review.domain.dto.ReviewCreateDTO;
import com.aynu.review.domain.dto.ReviewReportDTO;
import com.aynu.review.domain.dto.ReviewUpdateDTO;
import com.aynu.review.domain.po.ReviewStats;
import com.aynu.review.domain.po.Reviews;
import com.aynu.review.domain.vo.ReviewVO;
import com.aynu.review.mapper.ReviewsMapper;
import com.aynu.review.service.IReviewStatsService;
import com.aynu.review.service.IReviewsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价信息表，存储用户对租赁物品/交易的评价（逻辑外键关联物品/用户/订单表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements IReviewsService {

    private final IReviewStatsService reviewStatsService;
    private final UserClient userClient;

    @Override
    @Transactional
    public Long createReview(ReviewCreateDTO createDTO) {
        Long currentUserId = UserContext.getUser();

        // 检查是否已经评价过
        Reviews existReview = lambdaQuery().eq(Reviews::getOrderId, createDTO.getOrderId())
                .eq(Reviews::getReviewerId, currentUserId)
                .one();
        if (existReview != null) {
            throw new BadRequestException("该订单已评价过");
        }

        // 将图片列表转为JSON字符串
        String imagesJson = null;
        if (createDTO.getImages() != null && !createDTO.getImages().isEmpty()) {
            imagesJson = JSONUtil.toJsonStr(createDTO.getImages());
        }

        Reviews review = new Reviews();
        review.setItemId(createDTO.getItemId());
        review.setReviewerId(currentUserId);
        review.setTargetUserId(createDTO.getTargetUserId());
        review.setOrderId(createDTO.getOrderId());
        review.setRating(createDTO.getRating());
        review.setContent(createDTO.getContent());
        review.setImages(imagesJson);
        review.setStatus(1); // 正常
        review.setIsAnonymous(createDTO.getIsAnonymous() != null ? createDTO.getIsAnonymous() : false);
        review.setCreatedAt(System.currentTimeMillis());
        review.setUpdatedAt(System.currentTimeMillis());
        save(review);

        // 更新用户评价统计
        updateReviewStats(createDTO.getTargetUserId(), createDTO.getRating(), true);

        return review.getId();
    }

    @Override
    public PageDTO<ReviewVO> getItemReviews(Long itemId, PageQuery query) {
        Page<Reviews> page = lambdaQuery().eq(Reviews::getItemId, itemId)
                .eq(Reviews::getStatus, 1)
                .orderByDesc(Reviews::getCreatedAt)
                .page(query.toMpPage());

        List<Reviews> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        List<ReviewVO> list = convertToVOList(records);
        return PageDTO.of(page, list);
    }

    @Override
    public PageDTO<ReviewVO> getUserReviews(Long userId, PageQuery query) {
        Page<Reviews> page = lambdaQuery().eq(Reviews::getTargetUserId, userId)
                .eq(Reviews::getStatus, 1)
                .orderByDesc(Reviews::getCreatedAt)
                .page(query.toMpPage());

        List<Reviews> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        List<ReviewVO> list = convertToVOList(records);
        return PageDTO.of(page, list);
    }

    @Override
    public PageDTO<ReviewVO> getMyReviews(PageQuery query) {
        Long currentUserId = UserContext.getUser();

        Page<Reviews> page = lambdaQuery().eq(Reviews::getReviewerId, currentUserId)
                .orderByDesc(Reviews::getCreatedAt)
                .page(query.toMpPage());

        List<Reviews> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        List<ReviewVO> list = convertToVOList(records);
        return PageDTO.of(page, list);
    }

    @Override
    @Transactional
    public ReviewVO updateReview(Long reviewId, ReviewUpdateDTO updateDTO) {
        Long currentUserId = UserContext.getUser();

        Reviews review = getById(reviewId);
        if (review == null) {
            throw new BadRequestException("评价不存在");
        }

        // 只能修改自己的评价
        if (!review.getReviewerId().equals(currentUserId)) {
            throw new BadRequestException("只能修改自己的评价");
        }

        if (updateDTO.getRating() != null) {
            review.setRating(updateDTO.getRating());
        }
        if (updateDTO.getContent() != null) {
            review.setContent(updateDTO.getContent());
        }
        if (updateDTO.getImages() != null) {
            String imagesJson = JSONUtil.toJsonStr(updateDTO.getImages());
            review.setImages(imagesJson);
        }
        if (updateDTO.getIsAnonymous() != null) {
            review.setIsAnonymous(updateDTO.getIsAnonymous());
        }
        review.setUpdatedAt(System.currentTimeMillis());
        updateById(review);

        return convertToVO(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Long currentUserId = UserContext.getUser();

        Reviews review = getById(reviewId);
        if (review == null) {
            throw new BadRequestException("评价不存在");
        }

        // 只能删除自己的评价
        if (!review.getReviewerId().equals(currentUserId)) {
            throw new BadRequestException("只能删除自己的评价");
        }

        // 软删除
        review.setStatus(2); // 已删除
        review.setUpdatedAt(System.currentTimeMillis());
        updateById(review);

        // 更新用户评价统计
        updateReviewStats(review.getTargetUserId(), review.getRating(), false);
    }

    @Override
    public Map<String, Object> canReview(Long orderId) {
        Long currentUserId = UserContext.getUser();

        // 检查是否已经评价过
        Reviews existReview = lambdaQuery().eq(Reviews::getOrderId, orderId)
                .eq(Reviews::getReviewerId, currentUserId)
                .one();

        boolean canReview = existReview == null;
        String reason = canReview ? null : "该订单已评价过";

        return Map.of("canReview", canReview, "reason", reason);
    }

    @Override
    @Transactional
    public Boolean reportReview(Long reviewId, ReviewReportDTO reportDTO) {
        Long currentUserId = UserContext.getUser();

        Reviews review = getById(reviewId);
        if (review == null) {
            throw new BadRequestException("评价不存在");
        }

        // TODO: 创建举报记录
        log.info("用户 {} 举报评价 {}, 原因: {}", currentUserId, reviewId, reportDTO.getReason());

        return true;
    }

    @Override
    public Map<String, Object> getReviewStats(Long userId) {
        ReviewStats stats = reviewStatsService.lambdaQuery().eq(ReviewStats::getUserId, userId).one();

        if (stats == null) {
            return Map.of("totalReviews",
                    0,
                    "avgRating",
                    BigDecimal.ZERO,
                    "oneStarCount",
                    0,
                    "twoStarCount",
                    0,
                    "threeStarCount",
                    0,
                    "fourStarCount",
                    0,
                    "fiveStarCount",
                    0);
        }

        return Map.of("totalReviews",
                stats.getTotalReviews(),
                "avgRating",
                stats.getAvgRating(),
                "oneStarCount",
                stats.getOneStarCount(),
                "twoStarCount",
                stats.getTwoStarCount(),
                "threeStarCount",
                stats.getThreeStarCount(),
                "fourStarCount",
                stats.getFourStarCount(),
                "fiveStarCount",
                stats.getFiveStarCount());
    }

    private List<ReviewVO> convertToVOList(List<Reviews> reviews) {
        if (CollUtil.isEmpty(reviews)) {
            return Collections.emptyList();
        }

        // 收集用户ID
        Set<Long> userIds = new HashSet<>();
        reviews.forEach(review -> {
            userIds.add(review.getReviewerId());
            userIds.add(review.getTargetUserId());
        });

        // 获取用户信息
        List<UserDTO> users = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        // 转换为VO
        return reviews.stream().map(review -> convertToVO(review, userMap)).collect(Collectors.toList());
    }

    private ReviewVO convertToVO(Reviews review) {
        Set<Long> userIds = new HashSet<>();
        userIds.add(review.getReviewerId());
        userIds.add(review.getTargetUserId());

        List<UserDTO> users = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));

        return convertToVO(review, userMap);
    }

    private ReviewVO convertToVO(Reviews review, Map<Long, UserDTO> userMap) {
        ReviewVO vo = BeanUtil.toBean(review, ReviewVO.class);

        // 解析图片JSON
        if (StrUtil.isNotBlank(review.getImages())) {
            vo.setImages(JSONUtil.toList(review.getImages(), String.class));
        }

        // 设置用户信息
        UserDTO reviewer = userMap.get(review.getReviewerId());
        if (reviewer != null) {
            vo.setReviewerName(reviewer.getUsername());
            vo.setReviewerAvatar(reviewer.getAvatarUrl());
        }

        UserDTO targetUser = userMap.get(review.getTargetUserId());
        if (targetUser != null) {
            vo.setTargetUserName(targetUser.getUsername());
            vo.setTargetUserAvatar(targetUser.getAvatarUrl());
        }

        return vo;
    }

    private void updateReviewStats(Long userId, Integer rating, boolean isAdd) {
        ReviewStats stats = reviewStatsService.lambdaQuery().eq(ReviewStats::getUserId, userId).one();

        long now = System.currentTimeMillis();

        if (stats == null) {
            if (!isAdd) return; // 删除时不存在统计数据则不处理

            stats = new ReviewStats();
            stats.setUserId(userId);
            stats.setTotalReviews(1);
            stats.setAvgRating(BigDecimal.valueOf(rating));
            setRatingCount(stats, rating, 1);
            stats.setCreatedAt(now);
            stats.setUpdatedAt(now);
            reviewStatsService.save(stats);
        } else {
            int totalReviews = stats.getTotalReviews();
            BigDecimal avgRating = stats.getAvgRating();

            if (isAdd) {
                totalReviews++;
                // 更新平均评分
                BigDecimal newAvg = avgRating.multiply(BigDecimal.valueOf(totalReviews - 1))
                        .add(BigDecimal.valueOf(rating))
                        .divide(BigDecimal.valueOf(totalReviews), 2, BigDecimal.ROUND_HALF_UP);
                stats.setAvgRating(newAvg);
                setRatingCount(stats, rating, getRatingCount(stats, rating) + 1);
            } else {
                totalReviews--;
                // 更新平均评分
                if (totalReviews > 0) {
                    BigDecimal newAvg = avgRating.multiply(BigDecimal.valueOf(totalReviews + 1))
                            .subtract(BigDecimal.valueOf(rating))
                            .divide(BigDecimal.valueOf(totalReviews), 2, BigDecimal.ROUND_HALF_UP);
                    stats.setAvgRating(newAvg);
                } else {
                    stats.setAvgRating(BigDecimal.ZERO);
                }
                setRatingCount(stats, rating, getRatingCount(stats, rating) - 1);
            }
            stats.setTotalReviews(totalReviews);
            stats.setUpdatedAt(now);
            reviewStatsService.updateById(stats);
        }
    }

    private void setRatingCount(ReviewStats stats, Integer rating, Integer count) {
        switch (rating) {
            case 1:
                stats.setOneStarCount(count);
                break;
            case 2:
                stats.setTwoStarCount(count);
                break;
            case 3:
                stats.setThreeStarCount(count);
                break;
            case 4:
                stats.setFourStarCount(count);
                break;
            case 5:
                stats.setFiveStarCount(count);
                break;
        }
    }

    private Integer getRatingCount(ReviewStats stats, Integer rating) {
        switch (rating) {
            case 1:
                return stats.getOneStarCount();
            case 2:
                return stats.getTwoStarCount();
            case 3:
                return stats.getThreeStarCount();
            case 4:
                return stats.getFourStarCount();
            case 5:
                return stats.getFiveStarCount();
            default:
                return 0;
        }
    }
}
