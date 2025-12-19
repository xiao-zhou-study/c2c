package com.aynu.user.service;

import com.aynu.user.domain.po.UserProfiles;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户详细信息表，存储用户扩展信息（逻辑外键关联users表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
public interface IUserProfilesService extends IService<UserProfiles> {

}
