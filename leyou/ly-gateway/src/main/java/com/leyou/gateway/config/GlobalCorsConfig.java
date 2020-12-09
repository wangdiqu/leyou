package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 过滤器
 * 如果不想写死可移植到配置文件
 */
@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter(){
        System.out.println("---------------------------------------------------------------------------------------");
        //一、添加Cors配置信息
        CorsConfiguration config=new CorsConfiguration();
        //1.允许的域，不要写*，否则Cookie就无法使用了，
        config.addAllowedOrigin("http://manage.leyou.com");
        config.addAllowedOrigin("http://www.leyou.com");
        //config.addAllowedOrigin("http://api.leyou.com");
        //2.是否发送Cookie信息
        config.setAllowCredentials(true);
        //3.允许请求方式
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        //4.允许的头信息
        config.addAllowedHeader("*"); //Access-Control-Allow-Origin
        //5.添加有效时长
        config.setMaxAge(3600L);

        //二、添加映射路径，这里拦截一切请求
        UrlBasedCorsConfigurationSource configSource=new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**",config);
        //三、返回新的CorsFilter
        return new CorsFilter(configSource);
    }
}
