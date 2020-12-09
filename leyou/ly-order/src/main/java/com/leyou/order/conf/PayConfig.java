package com.leyou.order.conf;

import com.github.wxpay.sdk.WXPayConfig;
import lombok.Data;

import java.io.InputStream;
@Data
public class PayConfig implements WXPayConfig {
    private String appID; //公众账号ID
    private String mchID;//商户号
    private String key; //签名
    private int httpConnectTimeoutMs;
    private int httpReadTimeoutMs;
    private String notifyUrl;

    @Override
    public InputStream getCertStream() {
        return null;
    }
}
