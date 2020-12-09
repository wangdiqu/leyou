package com.leyou.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * 网关启动类
 */
@SpringCloudApplication
@EnableZuulProxy
public class LyGageway {
    public static void main(String[] args) {
        SpringApplication.run(LyGageway.class,args);
    }
}
