package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 库存
 */
@Data
@Table(name = "tb_stock")
public class Stock {
    @Id
    //@KeySql(useGeneratedKeys = true)
    private Long skuId;
    private Integer seckillStock;
    private Integer seckillTotal;
    private Integer stock;
}
