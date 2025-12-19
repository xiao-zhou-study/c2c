package com.aynu.user.mapper;

import com.aynu.user.domain.po.UserStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户统计信息表，存储用户行为相关的统计数据（逻辑外键关联users表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

}
