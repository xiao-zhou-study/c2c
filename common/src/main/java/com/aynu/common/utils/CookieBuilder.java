package com.aynu.common.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@Data
@Accessors(chain = true, fluent = true)
public class CookieBuilder {
    private Charset charset = StandardCharsets.UTF_8;
    private int maxAge = -1;
    private String path = "/";
    private boolean httpOnly;
    private String name;
    private String value;
    private String domain;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public CookieBuilder(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 构建cookie，会对cookie值用UTF-8做URL编码，避免中文乱码
     */
    public void build() {
        if (response == null) {
            log.error("response为null，无法写入cookie");
            return;
        }
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, charset));
        if (StringUtils.isNotBlank(domain)) {
            cookie.setDomain(domain);
        } else if (request != null) {
            String serverName = request.getServerName();
            // 只有当serverName是有效的域名（包含点号但不是IP地址）且不是localhost时，才设置跨子域名的cookie
            if (serverName != null && serverName.contains(".") && !serverName.equals("localhost") && !isIpAddress(
                    serverName)) {
                String domainName = StringUtils.subAfter(serverName, ".", false);
                if (domainName != null && !domainName.isEmpty()) {
                    cookie.setDomain("." + domainName);
                }
            }
            // 如果是localhost、IP地址或没有域名格式，不设置domain，使用默认的当前域名
        }
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        log.debug("生成cookie，编码方式:{}，【{}={}，domain:{};maxAge={};path={};httpOnly={}】",
                charset.name(),
                name,
                value,
                cookie.getDomain(),
                maxAge,
                path,
                httpOnly);
        response.addCookie(cookie);
    }

    /**
     * 判断是否为IP地址
     */
    private boolean isIpAddress(String serverName) {
        if (serverName == null) {
            return false;
        }
        // 简单判断是否为IP地址格式：只包含数字和点号，且每段都是数字
        String[] parts = serverName.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 利用UTF-8对cookie值解码，避免中文乱码问题
     *
     * @param cookieValue cookie原始值
     * @return 解码后的值
     */
    public String decode(String cookieValue) {
        return URLDecoder.decode(cookieValue, charset);
    }
}
