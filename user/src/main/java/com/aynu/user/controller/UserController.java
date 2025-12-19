package com.aynu.user.controller;

import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.user.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@Api(tags = "用户管理接口")
public class UserController {

    private final IUsersService usersService;

    @ApiOperation("用户注册")
    @PostMapping("register")
    public void register(@RequestBody UserDTO userDTO) {
        usersService.saveUser(userDTO);
    }

    @ApiOperation("查询用户详情")
    @PostMapping("detail/{isStaff}")
    public LoginUserDTO queryUserDetail(@RequestBody LoginFormDTO loginDTO, @PathVariable("isStaff") boolean isStaff) {
        return usersService.queryUserDetail(loginDTO, isStaff);
    }
}
