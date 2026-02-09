package com.aynu.campus.domain.vo;

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
public class CommentsVO implements Serializable {

    /**
     * 评论唯一ID
     */
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
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String username;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 评论时间戳(毫秒)
     */
    @Schema(description = "评论时间戳(毫秒)")
    private Long createTime;
}
