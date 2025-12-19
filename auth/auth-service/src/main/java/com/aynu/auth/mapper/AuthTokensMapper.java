package com.aynu.auth.mapper;

import com.aynu.auth.domain.po.AuthTokens;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 认证令牌表，存储用户的访问令牌和刷新令牌信息（时间字段为毫秒级时间戳） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Mapper
public interface AuthTokensMapper extends BaseMapper<AuthTokens> {

}
