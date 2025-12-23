package com.aynu.review.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "评价详情VO")
public class ReviewVO {

    @ApiModelProperty(value = "评价ID")
    private Long id;

    @ApiModelProperty(value = "物品ID")
    private Long itemId;

    @ApiModelProperty(value = "物品标题")
    private String itemTitle;

    @ApiModelProperty(value = "评价人ID")
    private Long reviewerId;

    @ApiModelProperty(value = "评价人用户名")
    private String reviewerName;

    @ApiModelProperty(value = "评价人头像")
    private String reviewerAvatar;

    @ApiModelProperty(value = "被评价人ID")
    private Long targetUserId;

    @ApiModelProperty(value = "被评价人用户名")
    private String targetUserName;

    @ApiModelProperty(value = "被评价人头像")
    private String targetUserAvatar;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "评分（1-5分）")
    private Integer rating;

    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "评价配图URL列表")
    private List<String> images;

    @ApiModelProperty(value = "评价状态：1-正常 2-已删除")
    private Integer status;

    @ApiModelProperty(value = "是否匿名评价")
    private Boolean isAnonymous;

    @ApiModelProperty(value = "评价创建时间戳")
    private Long createdAt;

    @ApiModelProperty(value = "评价更新时间戳")
    private Long updatedAt;
}
