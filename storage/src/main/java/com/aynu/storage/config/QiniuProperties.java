package com.aynu.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 七牛云配置属性
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@Component
@ConfigurationProperties(prefix = "qiniu")
public class QiniuProperties {

    /**
     * Access Key
     */
    private String ak;

    /**
     * Secret Key
     */
    private String sk;

    /**
     * 存储空间名称
     */
    private String bucket;

    /**
     * 访问域名
     */
    private String domain;
}
