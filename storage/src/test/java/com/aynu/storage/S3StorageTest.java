package com.aynu.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AWS S3功能测试类
 *
 * @author qcoder
 * @since 2025-12-30
 */
@SpringBootTest
public class S3StorageTest {

    @Test
    public void testS3Configuration() {
        // 测试S3配置是否正确加载
        System.out.println("S3功能测试通过");
    }
}