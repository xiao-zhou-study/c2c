package com.aynu.user.controller;

import com.aynu.api.client.storage.FileClient;
import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.user.domain.dto.PasswordChangeDTO;
import com.aynu.user.domain.dto.UserProfileDTO;
import com.aynu.user.domain.dto.VerifyDTO;
import com.aynu.user.domain.po.Users;
import com.aynu.user.domain.vo.UserProfileVO;
import com.aynu.user.domain.vo.UserStatsVO;
import com.aynu.user.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "用户管理接口")
public class UserController {

    private final IUsersService usersService;
    private final FileClient fileClient;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public void register(@RequestBody UserDTO userDTO) {
        usersService.saveUser(userDTO);
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
    public PageDTO<Users> queryUserPage(PageQuery query,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer status) {
        return usersService.queryUserPage(query, keyword, status);
    }

    @ApiOperation("新增管理人员")
    @PostMapping("/addStaff")
    public void addStaff(@RequestBody UserDTO userDTO) {
        usersService.addStaff(userDTO);
    }

    @ApiOperation("根据用户ID获取用户信息")
    @GetMapping("/{userId}")
    public UserDTO getUserById(@PathVariable("userId") Long userId) {
        return usersService.getUserById(userId);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable("userId") Long userId, @RequestBody UserDTO userDTO) {
        return usersService.updateUser(userId, userDTO);
    }

    @ApiOperation("获取用户详细资料")
    @GetMapping("/{userId}/profile")
    public UserProfileVO getUserProfile(@PathVariable("userId") Long userId) {
        return usersService.getUserProfile(userId);
    }

    @ApiOperation("更新用户详细资料")
    @PutMapping("/{userId}/profile")
    public UserProfileVO updateUserProfile(@PathVariable("userId") Long userId,
                                           @RequestBody UserProfileDTO profileDTO) {
        return usersService.updateUserProfile(userId, profileDTO);
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

    @ApiOperation("上传用户头像")
    @PostMapping("/avatar")
    public Map<String, String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = usersService.uploadAvatar(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return result;
    }
}