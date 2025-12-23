package com.aynu.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 文件存储根路径（云服务器上的路径）
     */
    private String basePath = "/usr/myprojects/image";

    /**
     * 文件访问URL前缀
     */
    private String urlPrefix = "http://117.72.74.175/images";

    /**
     * 允许上传的文件类型
     */
    private String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "avi", "pdf", "doc", "docx", "xls", "xlsx"};

    /**
     * 单个文件最大大小（MB）
     */
    private Long maxSize = 10L;

    /**
     * SFTP配置
     */
    private Sftp sftp = new Sftp();

    @Data
    public static class Sftp {
        /**
         * 云服务器主机地址
         */
        private String host;

        /**
         * SSH端口，默认22
         */
        private Integer port = 22;

        /**
         * 登录用户名
         */
        private String username;

        /**
         * 登录密码
         */
        private String password;

        /**
         * 连接超时时间（毫秒）
         */
        private Integer timeout = 30000;
    }
}
