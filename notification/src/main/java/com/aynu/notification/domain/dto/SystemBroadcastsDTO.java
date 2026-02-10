package com.aynu.notification.domain.dto;

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
public class SystemBroadcastsDTO implements Serializable {

    /**
     * 公告ID  id为null时是创建、id不为null时是更新
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
     * 是否有效：1-发布中, 0-已撤回/失效
     */
    @Schema(description = "是否有效：1-发布中, 0-已撤回/失效")
    private Boolean isActive;
}
