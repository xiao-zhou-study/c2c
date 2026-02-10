package com.aynu.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderNotifyMessage implements Serializable {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 订单Id
     */
    private String orderNo;

    /**
     * 消息类型 ： 1-借用消息 2-审核消息 3-归还消息
     */
    private Integer type;


}
