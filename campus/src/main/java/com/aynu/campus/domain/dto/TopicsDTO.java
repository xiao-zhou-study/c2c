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
public class TopicsDTO implements Serializable {
    /**
     * 话题唯一ID  id为null就是新增，否则就是修改
     */
    @Schema(description = "话题唯一ID")
    private Long id;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID")
    private Long categoryId;

    /**
     * 话题标题
     */
    @Schema(description = "话题标题")
    private String title;

    /**
     * 话题正文内容
     */
    @Schema(description = "话题正文内容")
    private String content;
}
