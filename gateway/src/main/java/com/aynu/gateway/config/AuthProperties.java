package com.aynu.gateway.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Data
@Component
@ConfigurationProperties(prefix = "xy.auth")
public class AuthProperties implements InitializingBean {

    private Set<String> excludePath = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 如果excludePath为空，则初始化默认值
        if (excludePath == null) {
            excludePath = new HashSet<>();
        }
        // 添加默认不拦截的路径（只有当集合中没有元素时才添加）
        if (excludePath.isEmpty()) {
            excludePath.add("/error/**");
            excludePath.add("/jwks");
            excludePath.add("/accounts/login");
            excludePath.add("/accounts/admin/login");
            excludePath.add("/accounts/refresh");
        }
    }
}
