package com.aynu.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BorrowOrdersCountDTO implements Serializable {
    /**
     * 订单状态Id
     */
    private Integer statusId;

    /**
     * 订单数量
     */
    private Long count;

}
