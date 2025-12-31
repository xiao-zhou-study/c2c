package com.aynu.storage.service.impl;

import cn.hutool.core.img.Img;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

        // 1. 准备文件数据
        byte[] fileBytes;
        try {
            // 尝试压缩图片，如果是图片则返回压缩后的字节，否则返回原字节
            fileBytes = compressImageIfNeeded(file, extension);
        } catch (IOException e) {
            log.error("文件读取或压缩失败", e);
            throw new BadRequestException("文件处理异常");
        }

        // 2. 重新计算关键信息（因为压缩后大小和MD5都变了）
        long finalFileSize = fileBytes.length;
        String md5 = DigestUtil.md5Hex(fileBytes);
        String fileName = IdUtil.simpleUUID() + (extension.isEmpty() ? "" : "." + extension);
        String fullPath = StrUtil.format("{}/{}", module, fileName);

        // 3. 上传 (使用处理后的字节流)
        String fileUrl;
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            fileUrl = s3Util.uploadFile(fullPath, is, mimeType);
        } catch (Exception e) {
            throw new BadRequestException("文件上传失败: " + e.getMessage());
        }

        // 4. 保存数据库记录
        Files fileEntity = new Files();
        fileEntity.setOriginalName(originalName);
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(fullPath);
        fileEntity.setFileSize(finalFileSize); // 记录实际存储的大小
        fileEntity.setFileType(getFileType(extension));
        fileEntity.setMimeType(mimeType);
        fileEntity.setMd5(md5); // 记录实际存储文件的MD5
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

    /**
     * 如果是图片，尝试压缩；否则返回原数据
     */
    private byte[] compressImageIfNeeded(MultipartFile file, String extension) throws IOException {
        // 定义需要压缩的图片类型 (排除 gif 动图，否则会变静态)
        List<String> compressTypes = Arrays.asList("jpg", "jpeg", "png", "bmp");

        // 如果不是目标图片类型，直接返回原字节
        if (!compressTypes.contains(extension.toLowerCase())) {
            return file.getBytes();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream in = file.getInputStream()) {

            // 使用 Hutool 压缩，0.8 的质量通常能减少 50% 体积且肉眼难辨
            // scale(1.0f) 表示尺寸不变，只调整质量
            Img.from(in).setQuality(0.8).write(out);

            byte[] compressedBytes = out.toByteArray();

            // 如果压缩后反而变大了（极少情况），或者压缩失败导致为空，则用原图
            if (compressedBytes.length == 0 || compressedBytes.length >= file.getSize()) {
                return file.getBytes();
            }

            log.info("图片压缩成功: {} -> {} (节省 {}%)",
                    FileUtil.readableFileSize(file.getSize()),
                    FileUtil.readableFileSize(compressedBytes.length),
                    (file.getSize() - compressedBytes.length) * 100 / file.getSize());

            return compressedBytes;
        } catch (Exception e) {
            log.warn("图片压缩失败，将使用原图上传: {}", e.getMessage());
            return file.getBytes();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("文件不能为空");

        if (file.getSize() > s3Properties.getMaxSize() * 1024 * 1024) {
            throw new BadRequestException("文件超过限制: " + s3Properties.getMaxSize() + "MB");
        }

        String ext = FileUtil.extName(file.getOriginalFilename());
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