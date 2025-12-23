package com.aynu.api.client.storage;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(value = "storage-service")
public interface FileClient {

    /**
     * 上传单个文件
     *
     * @param file   文件
     * @param module 业务模块
     * @return 文件URL
     */
    @PostMapping("/files/upload")
    String uploadFile(@RequestPart("file") MultipartFile file,
                      @RequestParam(value = "module", defaultValue = "common") String module);

    /**
     * 批量上传文件
     *
     * @param files  文件列表
     * @param module 业务模块
     * @return 文件URL列表
     */
    @PostMapping("/files/upload/batch")
    List<String> uploadFiles(@RequestPart("files") MultipartFile[] files,
                             @RequestParam(value = "module", defaultValue = "common") String module);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    @DeleteMapping("/files/{id}")
    Boolean deleteFile(@PathVariable("id") Long id);
}
