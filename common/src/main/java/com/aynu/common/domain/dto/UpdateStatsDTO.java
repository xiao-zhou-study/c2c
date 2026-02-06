package com.aynu.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStatsDTO implements Serializable {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 更新类型
     */
    private Integer type;

    /**
     * 是否增加
     */
    private Boolean isAdd;
}
