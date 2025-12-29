package com.aynu.api.enums.item;

import com.aynu.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum BillingType implements BaseEnum {
    PER_DAY(1, "per_day", "按天计费"),
    PER_WEEK(2, "per_week", "按周计费"),
    PER_MONTH(3, "per_month", "按月计费");

    @EnumValue
    final int value; // 数据库存储的整型值

    @JsonValue
    final String code; // JSON返回给前端的字符串标识

    final String desc; // 描述信息

    BillingType(int value, String code, String desc) {
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
    public static BillingType of(Object source) {
        if (source == null) {
            return null;
        }
        for (BillingType type : values()) {
            // 如果传入的是数字 (1, 2, 3)
            if (source instanceof Integer && type.value == (Integer) source) {
                return type;
            }
            // 如果传入的是字符串 ("per_day", "per_week"...)
            if (source instanceof String && type.code.equalsIgnoreCase((String) source)) {
                return type;
            }
        }
        return null;
    }
}