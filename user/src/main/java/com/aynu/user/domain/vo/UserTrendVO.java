package com.aynu.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTrendVO implements Serializable {

    /**
     * 日期
     */
    private String date;

    /**
     * 数量
     */
    private Long count;
}
