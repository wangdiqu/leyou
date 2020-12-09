package com.leyou.cart.pojo;

import lombok.Data;

@Data
public class Cart {
    private Long skuId;//商品ID
    private String title;//标题
    private String image;//图片路径
    private Long price;//价格
    private Integer num;//商品数量
    private String ownSpec;//商品规格参数
}
