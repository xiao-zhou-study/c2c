package com.aynu.campus.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 话题评论表 PO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("comments")
@Schema(description = "话题评论表对象")
public class CommentsPO implements Serializable {
    private static final long serialVersionUID = -72024500876410108L;

    /**
     * 评论唯一ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "评论唯一ID")
    private Long id;

    /**
     * 所属话题ID
     */
    @Schema(description = "所属话题ID")
    private Long topicId;

    /**
     * 发表评论的用户ID
     */
    @Schema(description = "发表评论的用户ID")
    private Long userId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 评论时间戳(毫秒)
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "评论时间戳(毫秒)")
    private Long createTime;

}
