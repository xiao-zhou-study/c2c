package com.aynu.review.controller;

import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.review.domain.dto.ReviewCreateDTO;
import com.aynu.review.domain.dto.ReportDTO;
import com.aynu.review.domain.dto.ReviewReportDTO;
import com.aynu.review.domain.dto.ReviewUpdateDTO;
import com.aynu.review.domain.po.Reviews;
import com.aynu.review.domain.vo.ReviewVO;
import com.aynu.review.service.IReviewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价信息表，存储用户对租赁物品/交易的评价（逻辑外键关联物品/用户/订单表） 前端控制器
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Api(tags = "评价管理接口")
public class ReviewsController {

    private final IReviewsService reviewsService;
    private final UserClient userClient;

    @ApiOperation("创建评价")
    @PostMapping("/reviews")
    public Long createReview(@Valid @RequestBody ReviewCreateDTO createDTO) {
        return reviewsService.createReview(createDTO);
    }

    @ApiOperation("获取物品评价列表")
    @GetMapping("/item/{itemId}")
    public PageDTO<ReviewVO> getItemReviews(@PathVariable("itemId") Long itemId, PageQuery query) {
        return reviewsService.getItemReviews(itemId, query);
    }

    @ApiOperation("获取用户收到的评价列表")
    @GetMapping("/user/{userId}")
    public PageDTO<ReviewVO> getUserReviews(@PathVariable("userId") Long userId, PageQuery query) {
        return reviewsService.getUserReviews(userId, query);
    }

    @ApiOperation("获取我发出的评价列表")
    @GetMapping("/my-reviews")
    public PageDTO<ReviewVO> getMyReviews(PageQuery query) {
        return reviewsService.getMyReviews(query);
    }

    @ApiOperation("更新评价")
    @PutMapping("/reviews/{reviewId}")
    public ReviewVO updateReview(@PathVariable("reviewId") Long reviewId, @Valid @RequestBody ReviewUpdateDTO updateDTO) {
        return reviewsService.updateReview(reviewId, updateDTO);
    }

    @ApiOperation("删除评价")
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(@PathVariable("reviewId") Long reviewId) {
        reviewsService.deleteReview(reviewId);
    }

    @ApiOperation("检查是否可以评价")
    @GetMapping("/can-review/{orderId}")
    public Map<String, Object> canReview(@PathVariable("orderId") Long orderId) {
        return reviewsService.canReview(orderId);
    }

    @ApiOperation("举报评价")
    @PostMapping("/reviews/{reviewId}/report")
    public Boolean reportReview(@PathVariable("reviewId") Long reviewId, @Valid @RequestBody ReviewReportDTO reportDTO) {
        return reviewsService.reportReview(reviewId, reportDTO);
    }

    @ApiOperation("获取评价统计")
    @GetMapping("/reviews/stats/{userId}")
    public Map<String, Object> getReviewStats(@PathVariable("userId") Long userId) {
        return reviewsService.getReviewStats(userId);
    }
}
