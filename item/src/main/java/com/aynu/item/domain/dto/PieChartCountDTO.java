package com.aynu.item.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PieChartCountDTO implements Serializable {
    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类下物品数量
     */
    private Integer value;
}
