package com.aynu.api.enums.item;

import com.aynu.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ItemStatus implements BaseEnum {
    AVAILABLE(1, "可借用"),
    BORROWED(2, "已借出"),
    OFF_SHELF(3, "已下架");

    @JsonValue
    @EnumValue
    final int value;
    final String desc;

    ItemStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ItemStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ItemStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
}