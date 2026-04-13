package com.aynu.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReviewDTO implements Serializable {

    /**
     * 订单编号
     */
    private String id;

    /**
     * 评分（1-5分）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价配图URL列表
     */
    private List<String> images;

    /**
     * 是否匿名评价
     */
    private Boolean isAnonymous;
}
