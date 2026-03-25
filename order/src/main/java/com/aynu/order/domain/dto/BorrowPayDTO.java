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
public class BorrowPayDTO implements Serializable {

    /**
     * 订单编号
     */
    private String id;

    /**
     * 版本号
     */
    private Integer version;


}
