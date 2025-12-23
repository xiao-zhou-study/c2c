package com.aynu.storage.mapper;

import com.aynu.storage.domain.po.Files;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件信息表，存储文件上传后的核心信息（逻辑外键关联用户/业务模块表） Mapper 接口
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Mapper
public interface FilesMapper extends BaseMapper<Files> {

}
