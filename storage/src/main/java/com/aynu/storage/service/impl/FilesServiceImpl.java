package com.aynu.storage.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.storage.config.FileUploadProperties;
import com.aynu.storage.domain.po.Files;
import com.aynu.storage.mapper.FilesMapper;
import com.aynu.storage.service.IFilesService;
import com.aynu.storage.util.QiniuUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 文件信息表，存储文件上传后的核心信息（逻辑外键关联用户/业务模块表） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilesServiceImpl extends ServiceImpl<FilesMapper, Files> implements IFilesService {

    private final FileUploadProperties uploadProperties;
    private final QiniuUtil qiniuUtil;

    @Override
    public String uploadFile(MultipartFile file, String module) {
        // 1. 校验文件
        validateFile(file);

        // 2. 获取文件信息
        String originalName = file.getOriginalFilename();
        String extension = FileUtil.extName(originalName);
        String mimeType = file.getContentType();
        long fileSize = file.getSize();

        // 3. 生成存储文件名（UUID + 扩展名）
        String fileName = IdUtil.simpleUUID() + "." + extension;
        String fullFileName = module + "/" + fileName;

        // 4. 计算MD5
        String md5;
        try {
            md5 = DigestUtil.md5Hex(file.getInputStream());
        } catch (IOException e) {
            log.error("计算文件MD5失败", e);
            throw new BadRequestException("文件处理失败");
        }

        // 5. 通过七牛云上传文件
        try (InputStream inputStream = file.getInputStream()) {
            String fileUrl = qiniuUtil.uploadFile(inputStream, fullFileName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BadRequestException("文件上传失败");
        }

        // 6. 构建文件记录
        Files fileEntity = new Files();
        fileEntity.setOriginalName(originalName);
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(fullFileName);
        fileEntity.setFileSize(fileSize);
        fileEntity.setFileType(getFileType(extension));
        fileEntity.setMimeType(mimeType);
        fileEntity.setMd5(md5);
        fileEntity.setUrl(qiniuUtil.getFileUrl(fullFileName)); // 使用七牛云返回的URL
        fileEntity.setUploaderId(UserContext.getUser());
        fileEntity.setModule(module);
        fileEntity.setStatus(1);
        fileEntity.setCreatedAt(System.currentTimeMillis());
        fileEntity.setUpdatedAt(System.currentTimeMillis());

        // 7. 保存到数据库
        save(fileEntity);

        // 8. 返回结果
        return qiniuUtil.getFileUrl(fullFileName);
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String module) {
        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(uploadFile(file, module));
        }
        return result;
    }

    @Override
    public boolean deleteFile(Long id) {
        Files file = getById(id);
        if (file == null) {
            throw new BadRequestException("文件不存在");
        }

        // 从七牛云删除文件
        try {
            qiniuUtil.deleteFile(file.getFilePath());
        } catch (Exception e) {
            log.error("删除七牛云文件失败", e);
        }

        // 软删除
        file.setStatus(2);
        file.setUpdatedAt(System.currentTimeMillis());
        return updateById(file);
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("上传文件不能为空");
        }

        // 校验文件大小
        long maxBytes = uploadProperties.getMaxSize() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("文件大小超过限制，最大允许" + uploadProperties.getMaxSize() + "MB");
        }

        // 校验文件类型
        String extension = FileUtil.extName(file.getOriginalFilename());
        if (extension == null || !Arrays.asList(uploadProperties.getAllowedTypes()).contains(extension.toLowerCase())) {
            throw new BadRequestException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 根据扩展名获取文件类型
     */
    private String getFileType(String extension) {
        if (extension == null) {
            return "other";
        }
        extension = extension.toLowerCase();
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension)) {
            return "image";
        } else if (Arrays.asList("mp4", "avi", "mov", "wmv", "flv").contains(extension)) {
            return "video";
        } else if (Arrays.asList("mp3", "wav", "flac", "aac").contains(extension)) {
            return "audio";
        } else if (Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt").contains(extension)) {
            return "document";
        }
        return "other";
    }
}
