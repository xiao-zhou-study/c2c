package com.aynu.auth.mapper;

import com.aynu.auth.domain.po.UserRoles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户角色关联表，实现用户与角色的多对多关联（时间字段为毫秒级时间戳，使用逻辑外键） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Mapper
public interface UserRolesMapper extends BaseMapper<UserRoles> {

}
