package com.aynu.campus.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 校园公告极简表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("campus_announcements")
@Schema(description = "校园公告极简表对象")
public class CampusAnnouncementsPO implements Serializable {
    private static final long serialVersionUID = -34378689303552962L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 创建时间（时间戳）
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间（时间戳）")
    private Long createTime;

    /**
     * 更新时间（时间戳）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间（时间戳）")
    private Long updateTime;

}
