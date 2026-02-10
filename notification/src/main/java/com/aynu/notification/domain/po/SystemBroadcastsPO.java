package com.aynu.notification.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全员广播公告内容表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("system_broadcasts")
@Schema(description = "全员广播公告内容表对象")
public class SystemBroadcastsPO implements Serializable {
    @Serial
    private static final long serialVersionUID = 751621927826783616L;

    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
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
    @Schema(description = "类型枚举：1-announcement, 2-activity, 3-maintenance, 4-system")
    private Integer category;

    /**
     * 接收范围：1-全员(All), 2-个人(Specific)
     */
    @Schema(description = "接收范围：1-全员(All), 2-个人(Specific)")
    private Integer targetType;

    /**
     * 目标用户ID（当target_type为2时必填）
     */
    @Schema(description = "目标用户ID（当target_type为2时必填）")
    private Long targetUserId;

    /**
     * 是否有效：1-发布中, 0-已撤回/失效
     */
    @Schema(description = "是否有效：1-发布中, 0-已撤回/失效")
    private Boolean isActive;

    /**
     * 发布时间戳（毫秒）
     */
    @Schema(description = "发布时间戳（毫秒）")
    private Long createdAt;

    /**
     * 更新时间戳（毫秒）
     */
    @Schema(description = "更新时间戳（毫秒）")
    private Long updatedAt;

}
