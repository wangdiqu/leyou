package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    IMAGE_CONTENT_ERROR(500,"图片内容不正确"),
    IMAGE_TYPE_ERROR(500,"图片格式不正确"),
    IMAGE_ERROR(500,"图片上传失败"),
    BRAND_UPDATE_ERROR(500,"修改数据失败"),
    BRAND_DELETE_FOUND(500,"删除失败！"),
    BRAND_INSERT_ERROR(500,"添加数据失败！"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    CATEGORY_NOT_FOUND(404,"商品分类不存在"),
    SPEC_GROUP_FOUND(404,"商品规格组不存在！"),
    SPEC_PARAM_ERROR(404,"商品规格参数不存在！"),
    GOODS_ID_NOT_NULL(400,"ID不存在！"),
    GOODS_SPU_NOT_FOUND(404,"商品不存在！"),
    GOODS_SPU_DETAIL_NOT_FOUND(404,"商品详情不存在！"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在！"),
    GOODS_UNDER_STOCK(500,"商品库存不够！"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU不存在！"),
    USER_DATA_TYPE(400,"无效的用户数据类型"),
    USERNAME_OR_PASSWORD_ERROR(404,"用户名或密码错误"),
    INVALID_USERNAME_PASSWORD(404,"token已过期或被篡改"),
    NO_AUTHORIZED(404,"无授权"),
    NO_CART_FOUND(404,"购物车无授权"),
    CREATE_ORDER_ERROR(500,"生成订单失败"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    ORDER_STATUS_ERROR(400,"订单状态不正常！"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败"),
    INVALID_SIGN_ERROR(400,"无效的签名异常"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
    UPDATE_ORDER_STARTUS_ERROR(400,"修改订单状态失败"),
    ;
    private int code;
    private String msg;

}
