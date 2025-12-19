package com.aynu.user.controller;

import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.user.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @ApiOperation("根据userId批量查询用户信息")
    @GetMapping("list")
    public List<UserDTO> queryUserByIds(@RequestParam("ids") Iterable<Long> ids) {
        return usersService.queryUserByIds(ids);
    }

    @ApiOperation("新增管理人员")
    @PostMapping("addStaff")
    public void addStaff(@RequestBody UserDTO userDTO) {
        usersService.addStaff(userDTO);
    }
}
