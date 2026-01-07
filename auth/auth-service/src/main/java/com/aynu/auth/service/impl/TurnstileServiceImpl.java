package com.aynu.auth.service.impl;

import com.aynu.auth.domain.vo.TurnstileVerificationResult;
import com.aynu.auth.service.ITurnstileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Cloudflare Turnstile 验证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnstileServiceImpl implements ITurnstileService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${turnstile.secret-key:0x4AAAAAACKmvWQqPlL_C5cfwWRINLL4k54}")
    private String secretKey;
    
    /**
     * 是否启用 Turnstile 验证，开发环境可设置为 false
     */
    @Value("${turnstile.enabled:true}")
    private boolean enabled;
    
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    
    @Override
    public TurnstileVerificationResult verifyToken(String token, String remoteip) {
        // 如果未启用 Turnstile 验证，直接返回成功（用于开发环境）
        if (!enabled) {
            log.debug("Turnstile verification is disabled, skipping validation");
            return TurnstileVerificationResult.success();
        }
        
        if (token == null || token.trim().isEmpty()) {
            log.warn("Turnstile token is null or empty");
            return TurnstileVerificationResult.failure(
                TurnstileVerificationResult.ErrorCode.INVALID_INPUT_RESPONSE.getCode(),
                "验证令牌不能为空"
            );
        }
        
        try {
            // 构建请求参数
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("secret", secretKey);
            requestBody.add("response", token);
            if (remoteip != null && !remoteip.trim().isEmpty()) {
                requestBody.add("remoteip", remoteip);
            }
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            log.debug("Calling Turnstile verification API with token length: {}", token.length());
            
            // 发送验证请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                TURNSTILE_VERIFY_URL, 
                request, 
                String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Turnstile API returned non-200 status: {}", response.getStatusCode());
                return TurnstileVerificationResult.failure(
                    TurnstileVerificationResult.ErrorCode.SERVICE_UNAVAILABLE.getCode(),
                    "验证服务不可用"
                );
            }
            
            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            boolean success = jsonResponse.get("success").asBoolean();
            TurnstileVerificationResult result = new TurnstileVerificationResult();
            result.setSuccess(success);
            result.setTimestamp(System.currentTimeMillis());
            
            if (jsonResponse.has("action")) {
                result.setAction(jsonResponse.get("action").asText());
            }
            
            if (jsonResponse.has("hostname")) {
                result.setHostname(jsonResponse.get("hostname").asText());
            }
            
            if (!success && jsonResponse.has("error-codes")) {
                JsonNode errorCodes = jsonResponse.get("error-codes");
                if (errorCodes.isArray() && !errorCodes.isEmpty()) {
                    String errorCode = errorCodes.get(0).asText();
                    result.setErrorCodes(errorCode);
                    
                    // 根据错误代码设置中文错误消息
                    String errorMessage = getErrorMessage(errorCode);
                    result.setMessage(errorMessage);
                    log.warn("Turnstile verification failed with error code: {}, message: {}", 
                        errorCode, errorMessage);
                }
            }
            
            log.info("Turnstile verification result: success={}, action={}, hostname={}", 
                success, result.getAction(), result.getHostname());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error verifying Turnstile token", e);
            return TurnstileVerificationResult.failure(
                TurnstileVerificationResult.ErrorCode.UNKNOWN_ERROR.getCode(),
                "验证失败，请稍后重试"
            );
        }
    }
    
    @Override
    public TurnstileVerificationResult verifyToken(String token) {
        String remoteip = getClientIpAddress();
        return verifyToken(token, remoteip);
    }
    
    /**
     * 获取客户端 IP 地址
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 按优先级尝试获取真实 IP
            String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP", 
                "X-Client-IP",
                "CF-Connecting-IP"
            };
            
            for (String header : headers) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    // X-Forwarded-For 可能包含多个 IP，取第一个
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    return ip;
                }
            }
            
            return request.getRemoteAddr();
            
        } catch (Exception e) {
            log.warn("Error getting client IP address", e);
            return null;
        }
    }
    
    /**
     * 根据错误代码获取中文错误消息
     */
    private String getErrorMessage(String errorCode) {
        TurnstileVerificationResult.ErrorCode error = 
            TurnstileVerificationResult.ErrorCode.fromCode(errorCode);
        return error.getDescription();
    }
}
