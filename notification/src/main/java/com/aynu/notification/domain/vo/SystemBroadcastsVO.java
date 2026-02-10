package com.aynu.notification.domain.vo;

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
public class SystemBroadcastsVO implements Serializable {
    /**
     * 公告ID
     */
    @Schema(description = "公告ID")
    private Long id;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题")
    private String title;

    /**
     * 公告正文
     */
    @Schema(description = "公告正文")
    private String content;

    /**
     * 类型枚举：1-announcement, 2-activity, 3-maintenance
     */
    @Schema(description = "类型枚举：1-announcement, 2-activity, 3-maintenance")
    private Integer category;

    /**
     * 发布时间戳（毫秒）
     */
    @Schema(description = "发布时间戳（毫秒）")
    private Long createdAt;

    /**
     * 当前用户已读状态
     */
    @Schema(description = "当前用户已读状态")
    private Boolean isRead;
}
