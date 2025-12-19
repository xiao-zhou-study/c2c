package com.aynu.user.mapper;

import com.aynu.user.domain.po.UserProfiles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户详细信息表，存储用户扩展信息（逻辑外键关联users表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Mapper
public interface UserProfilesMapper extends BaseMapper<UserProfiles> {

}
