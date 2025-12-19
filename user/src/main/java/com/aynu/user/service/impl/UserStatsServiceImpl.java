package com.aynu.user.service.impl;

import com.aynu.user.domain.po.UserStats;
import com.aynu.user.mapper.UserStatsMapper;
import com.aynu.user.service.IUserStatsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户统计信息表，存储用户行为相关的统计数据（逻辑外键关联users表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Service
public class UserStatsServiceImpl extends ServiceImpl<UserStatsMapper, UserStats> implements IUserStatsService {

}
