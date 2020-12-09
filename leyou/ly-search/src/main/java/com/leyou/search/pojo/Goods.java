package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Map;
import java.util.Set;
@Data
@Document(indexName = "goods",type = "docs",shards = 1,replicas = 0)
public class Goods {
    @Id
    private Long id;//spuid
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String all;//所有被搜索的信息

    @Field(type = FieldType.Keyword,index = false)
    private String subTitle;//卖点

    private Long brandId;//品牌id
    private Long cid1;//一级分类id
    private Long cid2;//二级分类id
    private Long cid3;//三级分类id
    private Date createTime;//创建时间
    private Set<Long> price;//价格
    @Field(type = FieldType.Keyword,index = false)
    private String skus;//sku的信息json结构

    private Map<String, Object> specs;//可搜索的规格参数，key是参数名称，value是数值

}
