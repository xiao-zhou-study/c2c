package com.aynu.auth.service.impl;

import com.aynu.auth.domain.po.Roles;
import com.aynu.auth.domain.po.UserRoles;
import com.aynu.auth.mapper.RolesMapper;
import com.aynu.auth.service.IRolesService;
import com.aynu.auth.service.IUserRolesService;
import com.aynu.common.exceptions.BadRequestException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限角色表，定义系统中的角色类型（时间字段为毫秒级时间戳） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Service
@RequiredArgsConstructor
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles> implements IRolesService {

    private final IUserRolesService userRolesService;

    @Override
    public void deleteRole(Long id) {
        // 1. 检查角色是否存在
        Roles role = getById(id);
        if (role == null) {
            throw new BadRequestException("角色不存在");
        }

        // 2. 检查是否有用户关联了该角色
        boolean hasUserAssigned = userRolesService.lambdaQuery().eq(UserRoles::getRoleId, id).exists();

        if (hasUserAssigned) {
            throw new BadRequestException("该角色已被分配给用户，无法删除");
        }

        // 3. 执行删除操作
        boolean removed = removeById(id);
        if (!removed) {
            throw new BadRequestException("角色删除失败");
        }
    }
}
