package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("wxpay")
public class NotifyController {
    @Autowired
    private OrderService orderService;

    /**
     *微信支付成功回调
     * @param result
     * @return
     */
    @PostMapping(value = "/notify",produces = "application/xml")//produces是指返回的是xml
    public Map<String, String> hello(@RequestBody Map<String,String> result){
        //处理回调
        orderService.handleNotify(result);
        //记录日志
        log.info("[微信支付回调] 接受微信支付回调,结果:{}",result);
        //成功返回的数据
        Map<String,String> msg=new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }

}
