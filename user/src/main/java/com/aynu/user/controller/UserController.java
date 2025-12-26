package com.aynu.user.controller;

import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.utils.UserContext;
import com.aynu.user.domain.dto.PasswordChangeDTO;
import com.aynu.user.domain.dto.UserRegisterDTO;
import com.aynu.user.domain.dto.VerifyDTO;
import com.aynu.user.domain.vo.UserStatsVO;
import com.aynu.user.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "用户管理接口")
public class UserController {

    private final IUsersService usersService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDTO dto) {
        usersService.saveUser(dto);
    }

    @ApiOperation("查询用户详情")
    @PostMapping("/detail/{isStaff}")
    public LoginUserDTO queryUserDetail(@RequestBody LoginFormDTO loginDTO, @PathVariable("isStaff") boolean isStaff) {
        return usersService.queryUserDetail(loginDTO, isStaff);
    }

    @ApiOperation("根据userId批量查询用户信息")
    @GetMapping("/list")
    public List<UserDTO> queryUserByIds(@RequestParam("ids") Iterable<Long> ids) {
        return usersService.queryUserByIds(ids);
    }

    @ApiOperation("分页查询用户列表")
    @GetMapping("/page")
    public PageDTO<UserDTO> queryUserPage(PageQuery query,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status) {
        return usersService.queryUserPage(query, keyword, status);
    }

    @ApiOperation("根据用户ID获取用户信息")
    @GetMapping("/{userId:\\d+}")
    public UserDTO getUserById(@PathVariable("userId") Long userId) {
        return usersService.getUserById(userId);
    }

    @ApiOperation("获取用户个人信息")
    @GetMapping("/me")
    public UserDTO getUserInfo() {
        return usersService.getUserById(UserContext.getUser());
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable("userId") Long userId, @RequestBody UserDTO userDTO) {
        return usersService.updateUser(userId, userDTO);
    }

    @ApiOperation("获取用户统计信息")
    @GetMapping("/{userId}/stats")
    public UserStatsVO getUserStats(@PathVariable("userId") Long userId) {
        return usersService.getUserStats(userId);
    }

    @ApiOperation("修改密码")
    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        usersService.changePassword(passwordChangeDTO);
    }

    @ApiOperation("实名认证")
    @PostMapping("/verify")
    public void verifyUser(@Valid @RequestBody VerifyDTO verifyDTO) {
        usersService.verifyUser(verifyDTO);
    }
}