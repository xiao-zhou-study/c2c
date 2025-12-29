package com.aynu.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {

    APPLYING(1, "申请中"),
    CONFIRMED(2, "已确认"),
    BORROWING(3, "借用中"),
    RETURNED(4, "已归还"),
    CANCELLED(5, "已取消"),
    REJECTED(6, "已拒绝");

    /**
     * @EnumValue: 标记存储到数据库的值
     * @JsonValue: 标记前端 API 序列化输出的值
     */
    @EnumValue
    @JsonValue
    private final int value;

    private final String desc;

    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据数值获取对应的枚举（可选：用于手动转换）
     */
    public static OrderStatus of(Integer value) {
        if (value == null) return null;
        for (OrderStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
}