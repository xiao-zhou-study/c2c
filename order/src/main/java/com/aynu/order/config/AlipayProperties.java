package com.aynu.order.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.aynu.common.exceptions.BadRequestException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayProperties {

    private String privateKey;

    private String alipayPublicKey;

    private String appId;

    private String gatewayUrl;

    private String returnUrl;

    private String notifyUrl;

    private String format;

    private String charset;

    private String signType;


    @Bean
    public AlipayClient alipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(gatewayUrl);
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat(format);
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset(charset);
        alipayConfig.setSignType(signType);

        AlipayClient alipayClient;
        try {
            alipayClient = new DefaultAlipayClient(alipayConfig);
        } catch (Exception e) {
            throw new BadRequestException("支付宝配置错误");
        }
        return alipayClient;
    }

}
