package com.leyou.cart.conf;

import com.leyou.cart.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private JwtProperties jprop;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {//注册机InterceptorRegistry
        //拦截一切路径
        registry.addInterceptor(new UserInterceptor(jprop)).addPathPatterns("/**");
    }
}
