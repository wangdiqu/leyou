package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.conf.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.wxpay.sdk.WXPayConstants.FAIL;
import static com.github.wxpay.sdk.WXPayConstants.SUCCESS;

@Slf4j
@Component
public class PayHelper {
    @Autowired
    private WXPay wxPay;
    @Autowired
    private PayConfig config;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;

    public String creteOrder(Long orderId, Long totalPay, String desc) {
        try {
            Map<String, String> data = new HashMap<>();
            //商品描述
            data.put("body", desc);
            //订单号
            data.put("out_trade_no", orderId.toString());
            //金额，单位为分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端ip
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", config.getNotifyUrl());
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //利用工具完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);
            //通过标识判断是否成功
            isSuccess(result);
            //打印结果
            /*for (Map.Entry<String,String> entrty : result.entrySet()) {
                String key=entrty.getKey();
                System.out.println(key + (key.length() >= 8 ? "\t:" : "\t\t: ") + entrty.getValue());
            }*/
            //System.out.println("--------------------------------------------------------------------------");
            //下单成功，获取支付连接
            String url = result.get("code_url");
            return url;
        } catch (Exception e) {
            log.error("[微信下单失败] 创建预交易订单异常！", e);
            return null;
        }
    }

    /**
     * 判断通信和业务标识
     *
     * @param result
     */
    public void isSuccess(Map<String, String> result) {
        //通过标识(retrun_code和result_code)判断是否成功
        String retrunCode = result.get("retrun_code");
        if (FAIL.equals(retrunCode)) {
            //通信retrun_code失败，记录日志，抛出异常
            log.error("[微信下单通信失败] 原因{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
        //业务标识(result_code)
        String resultCode = result.get("result_code");
        if (FAIL.equals(resultCode)) {
            //业务result_code失败，记录日志，抛出异常
            log.error("[微信下单通信失败] 错误码{},错误原因{}", result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    /**
     * 签名校验
     *
     * @param data
     */
    public void isValidSign(Map<String, String> data) {
        //重新生成签名
        try {
            String sign1 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.MD5);
            // 和传过来的签名进行比较
            String sign = data.get("sign");
            if (!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)) {
                //签名有误，抛出异常
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }
        } catch (Exception e) {
            //签名有误，抛出异常
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }
    }

    public PayState queryPayState(Long orderId) {
        try {
            //组织请求参数
            Map<String, String> data = new HashMap<>();
            //订单号
            data.put("out_trade_no", orderId.toString());
            //查询状态
            Map<String, String> result = wxPay.orderQuery(data);
            //校验通信状态
            isSuccess(result);
            //校验签名
            isValidSign(result);
            //校验金额
            String totalFeeStr = result.get("total_fee");
            //获取订单号
            String outTradeNo = result.get("out_trade_no");
            if(StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(outTradeNo)){
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            //获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if(totalFee != /*order.getActualPay()*/ 1){
                //金额不符
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            /**
             * SUCCESS-支付成功
             * REFUND-转入退款
             * NOTPAY-未支付
             * CLOSED-已关闭
             * REVOKED-已撤销(刷卡支付)
             * USERPAYING-支付中
             * PAYERROR-其他
             */
            String state = result.get("trade_state");
            //支付成功
            if (SUCCESS.equals(state)) {
                //修改订单状态
                OrderStatus orderStatus=new OrderStatus();
                orderStatus.setStatus(OrderStatusEnum.PAYED.value());
                orderStatus.setOrderId(orderId);
                orderStatus.setPaymentTime(new Date());
                int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
                if (count != 1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STARTUS_ERROR);
                }
                return PayState.SUCCESS;
            }
            //未支付或正在支付
            if ("NOTPAY".equals(state) || "USERPAYING".equals(state)) {
                return PayState.NOT_PAY;
            }
            //支付失败
            return PayState.FAIL;
        } catch (Exception e) {
            return PayState.NOT_PAY;
        }
    }
}
