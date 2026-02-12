package com.aynu.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 通知消息类型枚举
 */
@Getter
public enum NotifyTypeEnum {

    /**
     * 借用消息
     */
    BORROW_MESSAGE(1, "borrow", "借用消息"),

    /**
     * 审核消息
     */
    REVIEW_MESSAGE(2, "review", "审核消息"),

    /**
     * 归还消息
     */
    RETURN_MESSAGE(3, "return", "归还消息");

    @JsonValue
    @EnumValue
    private final int value;

    private final String code;
    private final String desc;

    NotifyTypeEnum(int value, String code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据数值获取对应的枚举
     *
     * @param value 数值
     * @return 对应的枚举对象
     */
    public static NotifyTypeEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (NotifyTypeEnum type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}