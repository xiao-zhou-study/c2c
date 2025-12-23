package com.aynu.notification.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "通知详情VO")
public class NotificationVO {

    @ApiModelProperty(value = "通知ID")
    private Long id;

    @ApiModelProperty(value = "接收人ID")
    private Long receiverId;

    @ApiModelProperty(value = "发送人ID")
    private Long senderId;

    @ApiModelProperty(value = "发送人用户名")
    private String senderName;

    @ApiModelProperty(value = "发送人头像")
    private String senderAvatar;

    @ApiModelProperty(value = "消息标题")
    private String title;

    @ApiModelProperty(value = "消息正文内容")
    private String content;

    @ApiModelProperty(value = "消息类型：system-系统通知、borrow-借用相关、return-归还相关、review-评价相关等")
    private String type;

    @ApiModelProperty(value = "关联业务ID")
    private Long relatedId;

    @ApiModelProperty(value = "关联业务类型：item-物品、order-订单等")
    private String relatedType;

    @ApiModelProperty(value = "是否已读")
    private Boolean isRead;

    @ApiModelProperty(value = "已读时间戳")
    private Long readAt;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDeleted;

    @ApiModelProperty(value = "消息创建时间戳")
    private Long createdAt;

    @ApiModelProperty(value = "消息更新时间戳")
    private Long updatedAt;
}
