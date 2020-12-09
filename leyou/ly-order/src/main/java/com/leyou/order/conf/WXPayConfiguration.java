package com.leyou.order.conf;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WXPayConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "leyou.pay")
    public PayConfig payConfig(){
        return new PayConfig();
    }

    /**
     * 用bean的形式将WXPay注入到Spring中
     * @return
     */
    @Bean
    public WXPay wxPay(){
        return new WXPay(payConfig(), WXPayConstants.SignType.HMACSHA256);
    }
}
