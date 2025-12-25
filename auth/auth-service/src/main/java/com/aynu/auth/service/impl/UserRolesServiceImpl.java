package com.aynu.auth.service.impl;

import com.aynu.auth.domain.po.Roles;
import com.aynu.auth.domain.po.UserRoles;
import com.aynu.auth.mapper.RolesMapper;
import com.aynu.auth.mapper.UserRolesMapper;
import com.aynu.auth.service.IUserRolesService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色关联表，实现用户与角色的多对多关联（时间字段为毫秒级时间戳，使用逻辑外键） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles> implements IUserRolesService {

    private final RolesMapper rolesMapper;

    @Override
    public void addUserRole(Long id, String roleName) {
        LambdaQueryChainWrapper<Roles> wrapper = new LambdaQueryChainWrapper<>(rolesMapper);
        Roles role = wrapper.eq(Roles::getName, roleName).one();
        UserRoles userRoles = new UserRoles();
        userRoles.setUserId(id);
        userRoles.setRoleId(role.getId());
        save(userRoles);
    }
}
