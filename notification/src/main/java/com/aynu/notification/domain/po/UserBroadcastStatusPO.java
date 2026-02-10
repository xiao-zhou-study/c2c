package com.aynu.notification.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户公告已读状态表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("user_broadcast_status")
@Schema(description = "用户公告已读状态表对象")
public class UserBroadcastStatusPO implements Serializable {
    private static final long serialVersionUID = -69067316190778412L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "${column.comment}")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 关联的公告ID
     */
    @Schema(description = "关联的公告ID")
    private Long broadcastId;

    /**
     * 阅读时间戳（毫秒）
     */
    @Schema(description = "阅读时间戳（毫秒）")
    private Long readAt;

}
