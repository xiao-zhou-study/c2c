package com.aynu.auth.domain.vo;

import lombok.Data;

/**
 * Cloudflare Turnstile 验证结果
 */
@Data
public class TurnstileVerificationResult {
    
    /**
     * 验证是否成功
     */
    private boolean success;
    
    /**
     * 验证失败时的错误代码
     */
    private String errorCodes;
    
    /**
     * 验证失败时的错误消息
     */
    private String message;
    
    /**
     * 验证时间戳
     */
    private long timestamp;
    
    /**
     * 动作（Action）
     */
    private String action;
    
    /**
     * 域名（Host）
     */
    private String hostname;
    
    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        INVALID_INPUT_SECRET("invalid-input-secret", "密钥无效"),
        INVALID_INPUT_RESPONSE("invalid-input-response", "响应无效"),
        INVALID_REQUEST("invalid-request", "请求无效"),
        BAD_REQUEST("bad-request", "请求错误"),
        RATE_LIMITED("rate-limited", "请求频率过高"),
        SERVICE_UNAVAILABLE("service-unavailable", "服务不可用"),
        UNKNOWN_ERROR("unknown-error", "未知错误");
        
        private final String code;
        private final String description;
        
        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static ErrorCode fromCode(String code) {
            for (ErrorCode errorCode : values()) {
                if (errorCode.code.equals(code)) {
                    return errorCode;
                }
            }
            return UNKNOWN_ERROR;
        }
    }
    
    public static TurnstileVerificationResult success() {
        TurnstileVerificationResult result = new TurnstileVerificationResult();
        result.setSuccess(true);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    public static TurnstileVerificationResult failure(String errorCode, String message) {
        TurnstileVerificationResult result = new TurnstileVerificationResult();
        result.setSuccess(false);
        result.setErrorCodes(errorCode);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    public static TurnstileVerificationResult failure(ErrorCode errorCode) {
        return failure(errorCode.getCode(), errorCode.getDescription());
    }
}
