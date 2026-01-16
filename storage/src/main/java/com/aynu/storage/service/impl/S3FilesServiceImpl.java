package com.aynu.storage.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.storage.config.S3Properties;
import com.aynu.storage.domain.po.Files;
import com.aynu.storage.mapper.FilesMapper;
import com.aynu.storage.service.IFilesService;
import com.aynu.storage.util.S3Util;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FilesServiceImpl extends ServiceImpl<FilesMapper, Files> implements IFilesService {

    private final S3Properties s3Properties;
    private final S3Util s3Util;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, String module) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String extension = StrUtil.blankToDefault(FileUtil.extName(originalName), "");
        String mimeType = file.getContentType();

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new BadRequestException("文件处理异常");
        }

        long fileSize = fileBytes.length;
        String md5 = DigestUtil.md5Hex(fileBytes);
        String fileName = IdUtil.simpleUUID() + (extension.isEmpty() ? "" : "." + extension);
        String fullPath = StrUtil.format("{}/{}", module, fileName);

        String fileUrl;
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            fileUrl = s3Util.uploadFile(fullPath, is, mimeType);
        } catch (Exception e) {
            throw new BadRequestException("文件上传失败: " + e.getMessage());
        }

        Files fileEntity = new Files();
        fileEntity.setOriginalName(originalName);
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(fullPath);
        fileEntity.setFileSize(fileSize);
        fileEntity.setFileType(getFileType(extension));
        fileEntity.setMimeType(mimeType);
        fileEntity.setMd5(md5);
        fileEntity.setUrl(fileUrl);
        fileEntity.setUploaderId(UserContext.getUser());
        fileEntity.setModule(module);
        fileEntity.setStatus(1);
        fileEntity.setCreatedAt(System.currentTimeMillis());
        fileEntity.setUpdatedAt(System.currentTimeMillis());

        this.save(fileEntity);
        return fileUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> uploadFiles(MultipartFile[] files, String module) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(this.uploadFile(file, module));
        }
        return urls;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long id) {
        Files file = getById(id);
        if (file == null) throw new BadRequestException("文件不存在");

        try {
            s3Util.deleteFile(file.getFilePath());
        } catch (Exception e) {
            log.warn("物理文件删除失败: {}", file.getFilePath());
        }

        file.setStatus(2);
        file.setUpdatedAt(System.currentTimeMillis());
        return updateById(file);
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("文件不能为空");

        String ext = FileUtil.extName(file.getOriginalFilename());

        if (!StringUtils.hasText(ext)) {
            throw new BadRequestException("文件格式错误");
        }

        if (Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp").contains(ext.toLowerCase())) {
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BadRequestException("图片文件超过限制: 10MB");
            }
        }

        if (file.getSize() > s3Properties.getMaxSize() * 1024 * 1024) {
            throw new BadRequestException("文件超过限制: " + s3Properties.getMaxSize() + "MB");
        }

        boolean isAllowed = Arrays.stream(s3Properties.getAllowedTypes()).anyMatch(type -> type.equalsIgnoreCase(ext));
        if (!isAllowed) throw new BadRequestException("不支持的文件类型: " + ext);
    }

    private String getFileType(String extension) {
        if (StrUtil.isBlank(extension)) return "other";
        String ext = extension.toLowerCase();
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp").contains(ext)) return "image";
        if (Arrays.asList("mp4", "avi", "mov").contains(ext)) return "video";
        if (Arrays.asList("pdf", "doc", "docx", "xls", "xlsx").contains(ext)) return "document";
        return "other";
    }
}
