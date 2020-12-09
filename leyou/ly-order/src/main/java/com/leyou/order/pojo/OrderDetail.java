package com.leyou.order.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_order_detail")
public class OrderDetail {
        @Id
        @KeySql(useGeneratedKeys = true)
        private Long id;//订单详情id
        private Long orderId;//订单id
        private Long skuId;//sku商品id
        private Integer num;//购买数量
        private String title;//商品标题
        private String ownSpec;//商品动态属性键值集
        private Long price;//价格,单位：分
        private String image;//商品图片
}
