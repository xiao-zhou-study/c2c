package com.aynu.auth.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "管理员登录表单实体")
public class LoginAdminFormDTO implements Serializable {

    @NotNull(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", example = "admin", required = true)
    private String username;

    @NotNull(message = "密码不能为空")
    @ApiModelProperty(value = "密码", example = "123", required = true)
    private String password;

    @ApiModelProperty(value = "7天免密登录", example = "true")
    private Boolean rememberMe = false;
    
    @ApiModelProperty(value = "Turnstile 验证令牌", example = "0x4AAAAAACKmvc6zs8DzCX1u_xxx")
    private String turnstileToken;
}
