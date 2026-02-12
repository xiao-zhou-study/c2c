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
public class BorrowRejectDTO implements Serializable {
    /**
     * 订单Id
     */
    private String id;

    /**
     * 拒绝原因
     */
    private String reason;

    /**
     * 版本号
     */
    private Integer version;
}
