package com.aynu.auth.service.impl;

import com.aynu.auth.domain.po.UserRoles;
import com.aynu.auth.mapper.UserRolesMapper;
import com.aynu.auth.service.IUserRolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles> implements IUserRolesService {

}
