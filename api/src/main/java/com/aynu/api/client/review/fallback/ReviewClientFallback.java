package com.aynu.api.client.review.fallback;

import com.aynu.api.client.review.ReviewClient;
import com.aynu.api.dto.review.ReviewCreateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Map;

@Slf4j
public class ReviewClientFallback implements FallbackFactory<ReviewClient> {

    @Override
    public ReviewClient create(Throwable cause) {
        log.error("调用评价服务异常", cause);
        return new ReviewClient() {

            @Override
            public Long createReview(ReviewCreateDTO dto) {
                return null;
            }

            @Override
            public Map<String, Object> canReview(String orderId) {
                return Map.of("canReview", false, "reason", "评价服务暂时不可用");
            }
        };
    }
}
