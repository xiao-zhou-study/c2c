package com.aynu.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO implements Serializable {

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户邮箱，唯一，用于登录/找回密码
     */
    private String email;

    /**
     * 密码哈希值（不可逆加密）
     */
    private String password;

    /**
     * 学号，唯一，用于登录/找回密码
     */
    private String studentId;

    /**
     * 所属学校
     */
    private String school;

    /**
     * 所属院系
     */
    private String department;

    /**
     * 年级（如2023级）
     */
    private String grade;

}
