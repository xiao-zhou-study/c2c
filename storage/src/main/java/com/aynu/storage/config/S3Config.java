package com.aynu.storage.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(name = "s3.access-key-id")
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        log.info("正在初始化 Cloudflare R2 客户端，端点: {}", s3Properties.getEndpoint());

        AwsBasicCredentials credentials = AwsBasicCredentials.create(s3Properties.getAccessKeyId(),
                s3Properties.getSecretAccessKey());

        S3Configuration serviceConf = S3Configuration.builder()
                .pathStyleAccessEnabled(s3Properties.getPathStyleAccessEnabled())
                .checksumValidationEnabled(false)
                .build();

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(s3Properties.getRegion()))
                .endpointOverride(URI.create(s3Properties.getEndpoint()))
                .serviceConfiguration(serviceConf)
                .build();
    }
}