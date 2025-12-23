package com.aynu.storage.controller;

import com.aynu.storage.service.IFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 文件信息表，存储文件上传后的核心信息（逻辑外键关联用户/业务模块表） 前端控制器
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
@Api(tags = "文件管理接口")
public class FilesController {

    private final IFilesService filesService;

    @ApiOperation("上传单个文件")
    @PostMapping("/upload")
    public String upload(@ApiParam("上传文件") @RequestParam("file") MultipartFile file,
                         @ApiParam("业务模块") @RequestParam(value = "module", defaultValue = "common") String module) {
        return filesService.uploadFile(file, module);
    }

    @ApiOperation("批量上传文件")
    @PostMapping("/upload/batch")
    public List<String> uploadBatch(@ApiParam("上传文件列表") @RequestParam("files") MultipartFile[] files,
                                    @ApiParam("业务模块") @RequestParam(value = "module",
                                            defaultValue = "common") String module) {
        return filesService.uploadFiles(files, module);
    }

    @ApiOperation("删除文件")
    @DeleteMapping("/{id}")
    public Boolean delete(@ApiParam("文件ID") @PathVariable Long id) {
        return filesService.deleteFile(id);
    }
}
