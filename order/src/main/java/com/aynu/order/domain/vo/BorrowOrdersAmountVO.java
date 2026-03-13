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
public class BorrowOrdersAmountVO implements Serializable {

    /**
     * 交易总额
     */
    private BigDecimal totalAmount;

    /**
     * 今日交易总额
     */
    private BigDecimal todayAmount;
}
