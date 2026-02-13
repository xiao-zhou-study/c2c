package com.aynu.order;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * 订单中心启动类
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@MapperScan("com.aynu.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrderApplication.class, args);
        Environment env = context.getEnvironment();
        printLog(env);
    }

    private static void printLog(Environment env) {
        try {
            String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
            String serverPort = Optional.ofNullable(env.getProperty("server.port")).orElse("8080");
            String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
                    .filter(StringUtils::hasText)
                    .orElse("");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String appName = env.getProperty("spring.application.name", "Order-Service");
            String profiles = String.join(",", env.getActiveProfiles());

            // ANSI 颜色控制
            String GREEN = "\033[32;1m";
            String CYAN = "\033[36;1m";
            String YELLOW = "\033[33;1m";
            String RESET = "\033[0m";

            log.info("""
                
                ----------------------------------------------------------------------
                \t{}'{}'{} is running successfully!
                
                \t{}> Local:{}      {}://localhost:{}{}
                \t{}> External:{}   {}://{}:{}{}
                
                \t{}> Profile(s):{} {}
                ----------------------------------------------------------------------
                """,
                    GREEN, appName, RESET,
                    CYAN, RESET, protocol, serverPort, contextPath,
                    CYAN, RESET, protocol, hostAddress, serverPort, contextPath,
                    YELLOW, RESET, profiles.isEmpty() ? "default" : profiles
            );
        } catch (UnknownHostException e) {
            log.warn("Order Service log metadata resolution failed.");
        }
    }
}