package com.aynu.campus.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class TopicsVO implements Serializable {
    /**
     * 话题唯一ID
     */
    @Schema(description = "话题唯一ID")
    private Long id;

    /**
     * 所属分类ID
     */
    @Schema(description = "所属分类ID")
    private Long categoryId;

    /**
     * 所属分类名称
     */
    @Schema(description = "所属分类名称")
    private String categoryName;

    /**
     * 发布话题的用户ID
     */
    @Schema(description = "发布话题的用户ID")
    private Long userId;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userNickname;

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
     * 评论量
     */
    @Schema(description = "评论量")
    private Integer commentCount;

    /**
     * 创建时间戳(毫秒)
     */
    @Schema(description = "创建时间戳(毫秒)")
    private Long createTime;

    /**
     * 更新时间戳(毫秒)
     */
    @Schema(description = "更新时间戳(毫秒)")
    private Long updateTime;
}
