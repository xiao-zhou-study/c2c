package com.aynu.order.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO implements Serializable {
    /**
     * 关联物品ID
     */
    @NotNull(message = "关联物品ID不能为空")
    @Schema(description = "关联物品ID")
    private Long itemId;

    /**
     * 预约借用开始时间戳（用户选择）
     */
    @NotNull(message = "预约借用开始时间戳不能为空")
    @Schema(description = "预约借用开始时间戳（用户选择）")
    private Long plannedStartTime;

    /**
     * 预约借用结束时间戳（用户选择）
     */
    @NotNull(message = "预约借用结束时间戳不能为空")
    @Schema(description = "预约借用结束时间戳（用户选择）")
    private Long plannedEndTime;

    /**
     * 借用用途说明
     */
    @NotNull(message = "借用用途说明不能为空")
    @Schema(description = "借用用途说明")
    private String purpose;

}
