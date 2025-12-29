package com.aynu.api.enums.item;

import com.aynu.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ConditionLevel implements BaseEnum {
    BRAND_NEW(0, "全新"),
    ALMOST_NEW(1, "九成新"),
    GENTLY_USED(2, "八成新");

    @JsonValue
    @EnumValue
    final int value;
    final String desc;

    ConditionLevel(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ConditionLevel of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ConditionLevel level : values()) {
            if (level.getValue() == value) {
                return level;
            }
        }
        return null;
    }
}