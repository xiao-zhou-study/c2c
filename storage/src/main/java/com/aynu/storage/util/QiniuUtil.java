package com.aynu.storage.util;

import com.aynu.common.exceptions.BadRequestException;
import com.aynu.storage.config.QiniuProperties;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 七牛云工具类
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QiniuUtil {

    private final QiniuProperties qiniuProperties;

    /**
     * 上传文件到七牛云
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名（包含扩展名）
     */
    public void uploadFile(InputStream inputStream, String fileName) {
        // 构建上传凭证
        Auth auth = Auth.create(qiniuProperties.getAk(), qiniuProperties.getSk());
        String upToken = auth.uploadToken(qiniuProperties.getBucket());

        try {
            // 创建上传配置 - 使用华北地区
            Configuration cfg = new Configuration(Region.huabei());
            UploadManager uploadManager = new UploadManager(cfg);

            // 上传文件
            uploadManager.put(inputStream, fileName, upToken, null, null);

        } catch (QiniuException ex) {
            Response r = ex.response;
            log.error("七牛云上传文件失败: {}", r.toString());
            try {
                log.error("七牛云上传错误详情: {}", r.bodyString());
            } catch (QiniuException e) {
                log.error("获取七牛云错误详情失败", e);
            }
            throw new BadRequestException("文件上传到七牛云失败: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("解析七牛云响应失败", ex);
            throw new BadRequestException("解析七牛云响应失败: " + ex.getMessage());
        }
    }

    /**
     * 删除七牛云上的文件
     *
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) {
        Auth auth = Auth.create(qiniuProperties.getAk(), qiniuProperties.getSk());
        Configuration cfg = new Configuration(Region.huabei());
        BucketManager bucketManager = new BucketManager(auth, cfg);

        try {
            bucketManager.delete(qiniuProperties.getBucket(), fileName);
            log.info("文件删除成功: {}", fileName);
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.error("七牛云删除文件失败: {}", r.toString());
            try {
                log.error("七牛云删除错误详情: {}", r.bodyString());
            } catch (QiniuException e) {
                log.error("获取七牛云错误详情失败", e);
            }
            throw new BadRequestException("删除七牛云文件失败: " + ex.getMessage());
        }
    }

    /**
     * 获取文件访问URL
     *
     * @param fileName 文件名
     * @return 文件访问URL
     */
    public String getFileUrl(String fileName) {
        return qiniuProperties.getDomain() + "/" + fileName;
    }
}
