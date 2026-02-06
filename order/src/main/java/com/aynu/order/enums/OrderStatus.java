package com.aynu.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

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

    @EnumValue
    @JsonValue
    private final int value;

    private final String desc;

    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 核心逻辑：判断当前状态是否允许取消
     * 规则：只有在“申请中”或“已确认（尚未取走物品）”时可以取消
     */
    public static boolean canCancel(Integer statusValue) {
        if (statusValue == null) return false;
        return Objects.equals(statusValue, APPLYING.value) || Objects.equals(statusValue, CONFIRMED.value);
    }

    /**
     * 根据数值获取对应的枚举
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