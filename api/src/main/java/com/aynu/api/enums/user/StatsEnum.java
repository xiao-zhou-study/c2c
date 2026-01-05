package com.aynu.api.enums.user;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户统计数据类型枚举
 */
@Getter
public enum StatsEnum {

    ITEMS_PUBLISHED(1, "发布物品数量", "items_published"),
    ITEMS_BORROWED(2, "借用物品数量", "items_borrowed"),
    ITEMS_LENT(3, "借出物品数量", "items_lent"),
    TOTAL_RATINGS(4, "累计被评价次数", "total_ratings");

    @EnumValue
    @JsonValue
    private final int value;

    private final String description;

    private final String dbField; // 数据库字段名

    StatsEnum(int value, String description, String dbField) {
        this.value = value;
        this.description = description;
        this.dbField = dbField;
    }
    
    /**
     * 支持通过 Integer形式的数字进行反序列化
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatsEnum of(Integer source) {
        if (source == null) {
            return null;
        }
        for (StatsEnum type : StatsEnum.values()) {
            if (type.value == source) {
                return type;
            }
        }
        return null;
    }
}