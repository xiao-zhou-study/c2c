package com.aynu.api.enums.order;

import com.aynu.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus implements BaseEnum {
    PENDING_CONFIRM(1, "pending_confirm", "待确认"),
    PENDING_PAYMENT(2, "pending_payment", "待付款"),
    IN_PROGRESS(3, "in_progress", "交易中/服务中"),
    PENDING_RETURN_CONFIRM(4, "pending_return_confirm", "待评价"),
    COMPLETED(5, "completed", "已完成"),
    CANCELLED(6, "cancelled", "已取消"),
    REJECTED(7, "rejected", "已拒绝"),
    IN_DISPUTE(8, "in_dispute", "争议中");

    @EnumValue
    final int value;

    @JsonValue
    final String code;

    final String desc;

    OrderStatus(int value, String code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    /**
     * 同时支持前端通过 code 字符串或 value 数字进行反序列化
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderStatus of(Object source) {
        if (source == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            // 如果传入的是数字 (1, 2, 3...)
            if (source instanceof Integer && status.value == (Integer) source) {
                return status;
            }
            // 如果传入的是字符串 ("pending_confirm", "pending_payment"...)
            if (source instanceof String && status.code.equalsIgnoreCase((String) source)) {
                return status;
            }
        }
        return null;
    }
}
