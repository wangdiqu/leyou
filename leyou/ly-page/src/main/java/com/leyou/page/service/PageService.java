package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.cilent.BrandClient;
import com.leyou.page.cilent.CategoryClient;
import com.leyou.page.cilent.GoodsClient;
import com.leyou.page.cilent.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model=new HashMap<>();
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //查询sku
        List<Sku> skus = spu.getSkus();
        //查询商品详情
        SpuDetail detail = spu.getSpuDetail();
        //查询商品品牌
        Brand brand = brandClient.queryByListCid(spu.getBrandId());
        //查询商品分类
        List<Category> categories = categoryClient.queryCategoryListByListIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询商品规格
        List<SpecGroup> specs = specificationClient.querySpecGroup(spu.getCid3());
         //map封装页面需要的所有数据  spu
        model.put("spu", spu);
        // 封装spuDetail
        model.put("detail", detail);
        // 封装sku集合
        model.put("skus", skus);
        // 分类
        model.put("categories", categories);
        // 品牌
        model.put("brand", brand);
        // 规格参数组
        model.put("specs", specs);
        // 查询特殊规格参数
       //model.put("paramMap", null);
        return model;
    }
    public void CreateHtml(Long spuId){
        //上下文
        Context context=new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File file=new File("D:/leyou/leyou-Upload",spuId+"html");
        if (file.exists()) {
            file.delete();
        }
        try(PrintWriter writer=new PrintWriter(file,"UTF-8")){
            //生成html
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("[静态页服务] 生成静态页异常！",e);
        }
    }
    public void deleteHtml(Long spuId) {
        File file=new File("D:/leyou/leyou-Upload",spuId+"html");
        if (file.exists()) {
            file.delete();
        }
    }
}
