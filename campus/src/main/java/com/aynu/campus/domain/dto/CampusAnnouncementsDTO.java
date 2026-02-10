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
public class CampusAnnouncementsDTO implements Serializable {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题")
    private String title;

    /**
     * 公告正文内容
     */
    @Schema(description = "公告正文内容")
    private String content;

    /**
     * 状态：0-下线，1-已发布
     */
    @Schema(description = "状态：0-下线，1-已发布")
    private Boolean isPublished;
}
