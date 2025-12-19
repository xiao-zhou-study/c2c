package com.aynu.user.mapper;

import com.aynu.user.domain.po.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户基本信息表，存储用户核心登录和基础信息 Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {

}
