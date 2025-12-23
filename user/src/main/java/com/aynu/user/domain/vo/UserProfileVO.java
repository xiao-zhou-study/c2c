package com.aynu.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户详细资料VO")
public class UserProfileVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "关联的用户ID")
    private Long userId;

    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "性别：1-男 2-女 0-保密")
    private Integer gender;

    @ApiModelProperty(value = "出生日期")
    private LocalDate birthday;

    @ApiModelProperty(value = "个人简介/个性签名")
    private String bio;

    @ApiModelProperty(value = "所属校区")
    private String campus;

    @ApiModelProperty(value = "宿舍号")
    private String dormitory;

    @ApiModelProperty(value = "QQ号码")
    private String qq;

    @ApiModelProperty(value = "微信号")
    private String wechat;

    @ApiModelProperty(value = "记录创建时间戳（毫秒级）")
    private Long createdAt;

    @ApiModelProperty(value = "记录更新时间戳（毫秒级）")
    private Long updatedAt;
}
