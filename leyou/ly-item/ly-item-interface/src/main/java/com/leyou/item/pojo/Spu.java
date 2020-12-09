package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * spu
 */
@Table(name = "tb_spu")
@Data
public class Spu {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long cid1;//一级类目
    private Long cid2;//二级类目
    private Long cid3;//三级类目
    private Long brandId;
    private String title;//标题
    private String subTitle;//子标题
    private Boolean saleable;//是否上架
    //返回页面数据时不包含此字段
    @JsonIgnore
    private Boolean valid;//是否有效，逻辑删除
    private Date createTime;//创建时间
    //返回页面数据时不包含此字段
    @JsonIgnore
    private Date lastUpdateTime;//修改时间

    @Transient
    private String cname;
    @Transient
    private String bname;
    @Transient
    private List<Sku> skus;
    @Transient
    private SpuDetail spuDetail;

}
