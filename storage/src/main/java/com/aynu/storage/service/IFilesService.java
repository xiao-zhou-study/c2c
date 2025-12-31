package com.aynu.storage.service;

import com.aynu.storage.domain.po.Files;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 文件信息表，存储文件上传后的核心信息（逻辑外键关联用户/业务模块表） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
public interface IFilesService extends IService<Files> {

    /**
     * 上传单个文件
     *
     * @param file   文件
     * @param module 业务模块
     * @return 文件上传结果
     */
    String uploadFile(MultipartFile file, String module) throws IOException;

    /**
     * 批量上传文件
     *
     * @param files  文件列表
     * @param module 业务模块
     * @return 文件上传结果列表
     */
    List<String> uploadFiles(MultipartFile[] files, String module) throws IOException;

    /**
     * 删除文件（软删除）
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean deleteFile(Long id);
}
