package com.leyou.search.pojo;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SearchResult extends PageResult<Goods> {
    private List<Category> categoryList;  //分类
    private List<Brand> brandList;     //品牌
    private List<Map<String, Object>> specs;   //规格参数key以及待选项


    public SearchResult(){ }
    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categoryList, List<Brand> brandList, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categoryList = categoryList;
        this.brandList = brandList;
        this.specs = specs;
    }
}
