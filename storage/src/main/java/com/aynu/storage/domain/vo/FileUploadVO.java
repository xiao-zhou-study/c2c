package com.aynu.storage.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件上传响应VO
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Data
@ApiModel(description = "文件上传响应")
public class FileUploadVO {

    @ApiModelProperty("文件ID")
    private Long id;

    @ApiModelProperty("原始文件名")
    private String originalName;

    @ApiModelProperty("存储文件名")
    private String fileName;

    @ApiModelProperty("文件访问URL")
    private String url;

    @ApiModelProperty("文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty("文件类型")
    private String fileType;

    @ApiModelProperty("MIME类型")
    private String mimeType;
}
