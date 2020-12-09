package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 商品详情
 */
@Table(name = "tb_spu_detail")
@Data
public class SpuDetail {
    @Id
    private Long spuId;
    private String description;//非空
    private String specTemplate;//非空
    private String genericSpec;
    private String specialSpec;
    private String packingList;
    private String afterService;
    private String specifications;

}
