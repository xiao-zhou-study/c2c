package com.aynu.api.config;

import com.aynu.api.client.item.fallback.ItemClientFallback;
import com.aynu.api.client.user.fallback.UserClientFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfig {

    @Bean
    public UserClientFallback userClientFallback() {
        return new UserClientFallback();
    }

    @Bean
    public ItemClientFallback itemClientFallback() {
        return new ItemClientFallback();
    }

}
