package com.aynu.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

    private String accessKeyId;
    private String secretAccessKey;
    private String region = "auto";
    private String bucketName;
    private String endpoint;
    private Boolean pathStyleAccessEnabled = true;
    private String urlPrefix;
    private String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "avi", "pdf", "doc", "docx", "xls", "xlsx"};
    private Long maxSize = 50L;
}