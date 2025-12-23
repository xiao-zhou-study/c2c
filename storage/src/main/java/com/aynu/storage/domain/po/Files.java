package com.aynu.storage.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 文件信息表，存储文件上传后的核心信息（逻辑外键关联用户/业务模块表）
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("files")
public class Files implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件原始名称（用户上传时的文件名）
     */
    private String originalName;

    /**
     * 文件存储名称（系统生成的唯一名称，避免重复）
     */
    private String fileName;

    /**
     * 文件存储路径（相对/绝对路径）
     */
    private String filePath;

    /**
     * 文件大小（单位：字节）
     */
    private Long fileSize;

    /**
     * 文件类型（如image、video、document）
     */
    private String fileType;

    /**
     * 文件MIME类型（如image/jpeg、application/pdf）
     */
    private String mimeType;

    /**
     * 文件MD5值，用于校验文件完整性/去重
     */
    private String md5;

    /**
     * 存储桶名称（如OSS/MinIO的bucket）
     */
    private String bucket;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * 上传人ID（逻辑外键，关联users表id）
     */
    private Long uploaderId;

    /**
     * 所属业务模块：item-物品、user-用户等
     */
    private String module;

    /**
     * 业务模块关联ID（逻辑外键，如module=item时关联items表id）
     */
    private Long moduleId;

    /**
     * 文件状态：1-正常 2-已删除（软删除）
     */
    private Integer status;

    /**
     * 文件上传时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 文件更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
