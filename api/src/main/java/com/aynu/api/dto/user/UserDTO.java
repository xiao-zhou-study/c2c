package com.aynu.api.dto.user;

import com.aynu.common.constants.RegexConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户详情")
public class UserDTO implements Serializable {
    @ApiModelProperty(value = "主键ID，使用雪花算法生成", example = "1234567890123456789")
    private Long id;

    @ApiModelProperty(value = "用户昵称", example = "zhangsan")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @ApiModelProperty(value = "用户邮箱，唯一，用于登录/找回密码", example = "zhangsan@example.com")
    @Email
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @ApiModelProperty(value = "手机号码，唯一，用户登录/找回密码", example = "13890017009")
    @Pattern(regexp = RegexConstants.PHONE_PATTERN, message = "手机号格式错误")
    private String phone;

    @ApiModelProperty(value = "密码，用于登录", example = "123456")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @ApiModelProperty(value = "头像图片URL地址", example = "default-user-icon.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    @ApiModelProperty(value = "学号，唯一，用于登录/找回密码", example = "2023001001")
    @Size(max = 50, message = "学号长度不能超过50个字符")
    private String studentId;

    @ApiModelProperty(value = "所属学校", example = "安阳师范学院")
    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    private String school;

    @ApiModelProperty(value = "所属院系", example = "计算机与信息工程学院")
    @Size(max = 100, message = "院系名称长度不能超过100个字符")
    private String department;

    @ApiModelProperty(value = "年级（如2023级）", example = "2023级")
    @Size(max = 20, message = "年级信息长度不能超过20个字符")
    private String grade;

    @ApiModelProperty(value = "信用分数，初始100分", example = "100")
    private Integer creditScore;

    @ApiModelProperty(value = "是否实名认证：FALSE-未认证，TRUE-已认证", example = "false")
    private Boolean isVerified;

    @ApiModelProperty(value = "账号状态：1-正常 2-禁用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "最后登录时间戳（毫秒级）", example = "1678886400000")
    private Long lastLoginAt;

    @ApiModelProperty(value = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @ApiModelProperty(value = "性别：1-男 2-女 0-保密", example = "1")
    private Integer gender;

    @ApiModelProperty(value = "出生日期", example = "2000-01-01")
    private LocalDate birthday;

    @ApiModelProperty(value = "个人简介/个性签名", example = "热爱编程，喜欢分享技术")
    @Size(max = 65535, message = "个人简介长度不能超过65535个字符")
    private String bio;

    @ApiModelProperty(value = "QQ号码", example = "123456789")
    @Size(max = 20, message = "QQ号码长度不能超过20个字符")
    private String qq;

    @ApiModelProperty(value = "微信号", example = "wechat123")
    @Size(max = 50, message = "微信号长度不能超过50个字符")
    private String wechat;

    @ApiModelProperty(value = "角色名称", example = "5")
    private String role;

    @ApiModelProperty(value = "类型", example = "1")
    private Integer type;

    @ApiModelProperty(value = "创建时间", example = "1888487782")
    private Long createdAt;

    @ApiModelProperty(value = "更新时间", example = "1888487782")
    private Long updatedAt;
}
