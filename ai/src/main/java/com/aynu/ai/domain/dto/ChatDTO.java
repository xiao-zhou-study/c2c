package com.aynu.ai.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO implements Serializable {

    @NotNull(message = "memoryId 不能为空")
    private String memoryId;

    @NotNull(message = "message 不能为空")
    private String message;
}