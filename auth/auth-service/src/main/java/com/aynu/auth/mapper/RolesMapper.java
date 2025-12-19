package com.aynu.auth.mapper;

import com.aynu.auth.domain.po.Roles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 权限角色表，定义系统中的角色类型（时间字段为毫秒级时间戳） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Mapper
public interface RolesMapper extends BaseMapper<Roles> {

}
