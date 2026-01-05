package com.aynu.storage.controller;

import com.aynu.storage.service.IFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Cloudflare R2 文件管理前端控制器
 *
 * @author qcoder
 * @since 2025-12-30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
@Api(tags = "Cloudflare R2存储接口")
public class S3FilesController {

    private final IFilesService s3FilesService;

    @ApiOperation("上传单个文件")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public String upload(@ApiParam(value = "文件对象", required = true) @RequestParam("file") MultipartFile file,
                         @ApiParam(value = "业务模块(如 avatar, goods)",
                                 defaultValue = "common") @RequestParam(value = "module",
                                 defaultValue = "common") String module) throws IOException {
        log.info("接收到单文件上传请求，文件名: {}, 模块: {}", file.getOriginalFilename(), module);
        return s3FilesService.uploadFile(file, module);
    }

    @ApiOperation("批量上传文件")
    @PostMapping(value = "/upload/batch", consumes = "multipart/form-data")
    public List<String> uploadBatch(@ApiParam(value = "文件列表",
            required = true) @RequestParam("files") MultipartFile[] files,
                                    @ApiParam(value = "业务模块",
                                            defaultValue = "common") @RequestParam(value = "module",
                                            defaultValue = "common") String module) throws IOException {
        log.info("接收到批量上传请求，文件数量: {}, 模块: {}", files.length, module);
        return s3FilesService.uploadFiles(files, module);
    }

    @ApiOperation("删除文件")
    @DeleteMapping("/{id}")
    public Boolean delete(@ApiParam(value = "数据库记录ID", required = true) @PathVariable Long id) {
        log.info("接收到文件删除请求，ID: {}", id);
        return s3FilesService.deleteFile(id);
    }
}
