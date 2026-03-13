package com.aynu.order.domain.vo;

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
public class BorrowOrdersPieVO implements Serializable {
    /**
     * 订单状态名称
     */
    private String name;

    /**
     * 订单状态比例
     */
    private BigDecimal value;

}
