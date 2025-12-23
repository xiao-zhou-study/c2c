package com.aynu.user.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel(description = "用户资料DTO")
public class UserProfileDTO {

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
}
