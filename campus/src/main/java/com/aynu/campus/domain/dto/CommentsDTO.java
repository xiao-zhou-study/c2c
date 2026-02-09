package com.aynu.campus.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentsDTO implements Serializable {
    /**
     * 所属话题ID
     */
    @Schema(description = "所属话题ID")
    private Long topicId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;
}
