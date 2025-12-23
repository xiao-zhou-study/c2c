package com.aynu.auth.controller;


import cn.hutool.core.collection.CollectionUtil;
import com.aynu.api.dto.auth.RoleDTO;
import com.aynu.auth.domain.po.Roles;
import com.aynu.auth.service.IRolesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


/**
 * @author 虎哥
 * @since 2022-06-16
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRolesService roleService;

    @ApiOperation("查询角色列表")
    @GetMapping("/list")
    public List<Roles> listAllRoles() {
        // 1.查询
        List<Roles> list = roleService.list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 3.数据转换
        return list;
    }

    @ApiOperation("根据id查询角色")
    @GetMapping("/{id}")
    public RoleDTO queryRoleById(@PathVariable("id") Long id) {
        // 1.查询
        Roles role = roleService.getById(id);
        if (role == null) {
            return null;
        }
        // 2.数据转换
        return role.toDTO();
    }


    @ApiOperation("新增角色")
    @PostMapping
    public RoleDTO saveRole(@RequestBody RoleDTO roleDTO) {
        Roles role = new Roles(roleDTO);
        role.setCreatedAt(System.currentTimeMillis());
        role.setUpdatedAt(System.currentTimeMillis());
        // 1.新增
        roleService.save(role);
        // 2.返回
        roleDTO.setId(role.getId());
        return roleDTO;
    }

    @ApiOperation("修改角色信息")
    @PutMapping("{id}")
    public void updateRole(@RequestBody RoleDTO roleDTO,
                           @ApiParam(value = "角色id", example = "1") @PathVariable("id") Long id) {
        // 1.数据转换
        Roles role = new Roles(roleDTO);
        role.setId(id);
        // 2.修改
        roleService.updateById(role);
    }

    @ApiOperation("删除角色信息")
    @DeleteMapping("{id}")
    public void deleteRole(@ApiParam(value = "角色id", example = "1") @PathVariable("id") Long id) {
        roleService.deleteRole(id);
    }
}
