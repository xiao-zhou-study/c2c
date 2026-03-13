package com.aynu.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BorrowOrdersTrendVO implements Serializable {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 订单数
     */
    private Long orderCount;

    /**
     * 交易额
     */
    private BigDecimal transactionAmount;

}
