package com.aynu.item.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PieChartVO implements Serializable {
    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类所占比例
     */
    private BigDecimal value;
}
