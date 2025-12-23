package com.aynu.notification.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 通知模板表，存储消息模板，用于动态生成通知内容
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("notification_templates")
public class NotificationTemplates implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码（唯一标识，如BORROW_APPLY、ORDER_CONFIRM）
     */
    private String code;

    /**
     * 模板名称（如“借用申请通知”）
     */
    private String name;

    /**
     * 标题模板（支持变量替换，如“【借用申请】{{title}}”）
     */
    private String titleTemplate;

    /**
     * 内容模板（支持变量替换，如“用户{{nickname}}申请借用您的{{item}}”）
     */
    private String contentTemplate;

    /**
     * 模板所属消息类型：system-系统、borrow-借用、return-归还等
     */
    private String type;

    /**
     * 是否启用：TRUE-启用、FALSE-禁用
     */
    private Boolean isActive;

    /**
     * 模板创建时间戳（毫秒级）
     */
    private Long createdAt;

    /**
     * 模板更新时间戳（毫秒级）
     */
    private Long updatedAt;


}
