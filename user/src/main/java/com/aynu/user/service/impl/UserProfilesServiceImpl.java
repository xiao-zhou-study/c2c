package com.aynu.user.service.impl;

import com.aynu.user.domain.po.UserProfiles;
import com.aynu.user.mapper.UserProfilesMapper;
import com.aynu.user.service.IUserProfilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户详细信息表，存储用户扩展信息（逻辑外键关联users表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Service
public class UserProfilesServiceImpl extends ServiceImpl<UserProfilesMapper, UserProfiles> implements IUserProfilesService {

}
