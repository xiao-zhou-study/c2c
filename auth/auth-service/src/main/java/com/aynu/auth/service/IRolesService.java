package com.aynu.auth.service;

import com.aynu.api.dto.auth.RoleDTO;
import com.aynu.auth.domain.po.Roles;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 权限角色表，定义系统中的角色类型（时间字段为毫秒级时间戳） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
public interface IRolesService extends IService<Roles> {

    void deleteRole(Long id);

    RoleDTO queryRoleByUserId(Long userId);
}
