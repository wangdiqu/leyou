package com.leyou.common.vo;

import lombok.Data;

import java.util.Date;
@Data
public class GoodsSpu {
    private Long id;
    private Long cid1;//一级类目
    private Long cid2;//二级类目
    private Long cid3;//三级类目
    private Long brand_id;
    private String title;//标题
    private String sub_title;//子标题
    private Boolean saleable;//是否上架
    private Boolean valid;//是否有效，逻辑删除
    private Date create_time;//创建时间
    private Date last_update_time;//修改时间
    private String cname;
    private String bname;
}
