package com.aynu.auth.service;

import com.aynu.auth.domain.vo.TurnstileVerificationResult;

/**
 * Cloudflare Turnstile 验证服务接口
 */
public interface ITurnstileService {

    /**
     * 验证 Turnstile 令牌
     *
     * @param token    前端传过来的 cf-turnstile-response 值
     * @param remoteip 用户的 IP 地址（可选）
     * @return 验证结果
     */
    TurnstileVerificationResult verifyToken(String token, String remoteip);

    /**
     * 简化版验证方法，只传入令牌
     *
     * @param token 前端传过来的 cf-turnstile-response 值
     * @return 验证结果
     */
    TurnstileVerificationResult verifyToken(String token);
}
