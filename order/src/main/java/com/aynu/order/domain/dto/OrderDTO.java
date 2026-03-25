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

}
