package com.aynu.api.client.review;

import com.aynu.api.client.review.fallback.ReviewClientFallback;
import com.aynu.api.dto.review.ReviewCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "review-service", fallbackFactory = ReviewClientFallback.class)
public interface ReviewClient {

    /**
     * 创建评价
     *
     * @param dto 评价创建参数
     * @return 评价ID
     */
    @PostMapping("/reviews/reviews")
    Long createReview(@RequestBody ReviewCreateDTO dto);

    /**
     * 检查是否可以评价
     *
     * @param orderId 订单ID
     * @return 是否可评价
     */
    @GetMapping("/reviews/can-review/{orderId}")
    Map<String, Object> canReview(@PathVariable("orderId") String orderId);
}
