package com.aynu.notification.enmus;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 系统广播分类枚举
 * 用于系统广播消息的分类管理
 */
@Getter
public enum CategoryEnum {

    ANNOUNCEMENT(1, "announcement", "公告通知"),
    ACTIVITY(2, "activity", "活动通知"),
    MAINTENANCE(3, "maintenance", "维护通知");

    @JsonValue
    @EnumValue
    private final int value;

    private final String code;
    private final String desc;

    CategoryEnum(int value, String code, String desc) {
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
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CategoryEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (CategoryEnum category : values()) {
            if (category.getValue() == value) {
                return category;
            }
        }
        return null;
    }
}