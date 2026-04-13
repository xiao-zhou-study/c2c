package com.aynu.api.dto.review;

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
public class ReviewCreateDTO implements Serializable {

    private Long itemId;

    private Long targetUserId;

    private String orderId;

    private Integer rating;

    private String content;

    private List<String> images;

    private Boolean isAnonymous;
}
