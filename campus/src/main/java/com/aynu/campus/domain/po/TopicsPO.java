package com.aynu.campus.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 讨论区话题表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("topics")
@Schema(description = "讨论区话题表对象")
public class TopicsPO implements Serializable {
    @Serial
    private static final long serialVersionUID = 284849897797921817L;

    /**
     * 话题唯一ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "话题唯一ID")
    private Long id;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID")
    private Long categoryId;

    /**
     * 发布话题的用户ID
     */
    @Schema(description = "发布话题的用户ID")
    private Long userId;

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

    /**
     * 浏览量
     */
    @Schema(description = "浏览量")
    private Long viewCount;

    /**
     * 创建时间戳(毫秒)
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间戳(毫秒)")
    private Long createTime;

    /**
     * 更新时间戳(毫秒)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间戳(毫秒)")
    private Long updateTime;

}
