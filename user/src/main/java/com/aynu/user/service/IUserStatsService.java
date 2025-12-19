package com.aynu.user.service;

import com.aynu.user.domain.po.UserStats;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户统计信息表，存储用户行为相关的统计数据（逻辑外键关联users表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
public interface IUserStatsService extends IService<UserStats> {

}
