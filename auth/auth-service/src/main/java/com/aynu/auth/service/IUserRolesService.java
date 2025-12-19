package com.aynu.auth.service;

import com.aynu.auth.domain.po.UserRoles;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户角色关联表，实现用户与角色的多对多关联（时间字段为毫秒级时间戳，使用逻辑外键） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
public interface IUserRolesService extends IService<UserRoles> {

}
