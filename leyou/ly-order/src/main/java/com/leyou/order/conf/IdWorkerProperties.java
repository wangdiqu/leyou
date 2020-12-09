package com.leyou.order.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "leyou.worker")
public class IdWorkerProperties {
    private Long workerId;//当前机器id
    private Long dataCenterId;//序列号
}
