package com.aynu.item.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 物品状态更新请求DTO
 */
@Data
public class ItemStatusUpdateDTO {

    /**
     * 物品状态：1-可借用 2-已借出 3-已下架
     */
    @NotNull(message = "物品状态不能为空")
    private Integer status;

    /**
     * 操作备注
     */
    private String remark;
}