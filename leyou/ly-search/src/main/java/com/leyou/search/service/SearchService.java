package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository repository;
    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        //查询分类
        List<Category> categories = categoryClient.queryCategoryListByListIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> collect = categories.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryByListCid(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all = spu.getTitle() + StringUtils.join(collect, " ") + brand.getName();

        //查询sku
        List<Sku> skuList = goodsClient.querySkuList(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        Set<Long> priceSet = new HashSet<>();
        //对sku进行处理
        List<Map<String, Object>> objects = new ArrayList<>();
        for (Sku skus : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skus.getId());
            map.put("title", skus.getTitle());
            map.put("price", skus.getPrice());
            map.put("image", skus.getImages());
            objects.add(map);
            //处理价格
            priceSet.add(skus.getPrice());
        }
        //查询规格参数
        List<SpecParam> specParams = specificationClient.querySpecParam(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_ERROR);
        }
        System.err.println(spuId+"---------------");
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);
        //获取通用规格
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特殊规格
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //查询规格
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : specParams) {
            //建
            String key = param.getName();
            Object value = "";
            //判断是否是通用规格
            if (param.getGeneric()) {
                value = genericSpec.get(param.getId());
                //判断是否是数值类型
                if (param.getNumeric()) {
                    value = shooseSegsent(value.toString(), param);
                }
            } else {
                value = specialSpec.get(param.getId());
            }
            //存入Map
            specs.put(key, value);
        }
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setAll(all);  //  搜索字段，包含标题、品牌、规格、分类
        goods.setPrice(priceSet);// 所有sku价格的集合
        goods.setSkus(JsonUtils.toString(objects)); // 所有sku集合的json格式
        goods.setSpecs(specs); // 所有可搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String shooseSegsent(String value, SpecParam sp) {
        double val = NumberUtils.toDouble(value);
        String result = "其他";
        //保存竖直段
        for (String segments : sp.getSegments().split(",")) {
            String[] segment = segments.split("-");
            //获取数值范围
            double begin = NumberUtils.toDouble(segment[0]);
            double end = Double.MAX_VALUE;
            if (segment.length == 2) {
                end = NumberUtils.toDouble(segment[1]);
            }
            //判断是否在范围内
            if (val >= begin && val < end) {
                if (segment.length == 1) {
                    result = segment[0] + sp.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segment[1] + sp.getUnit() + "以下";
                } else {
                    result = segments + sp.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        int page=request.getPage()-1;
        int size=request.getSize();
        //创建查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        //搜索条件
        QueryBuilder basicQuery = buildBasicQuery(request);
                //QueryBuilders.matchQuery("all", request.getKey());
        //过滤
        queryBuilder.withQuery(basicQuery);
        //聚合，分类和品牌
        //聚合分类
        String categoryAggName="Category_AggName";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //聚合品牌
        String brandAggName="Brand_AggName";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析分页结果
        long totals = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        if(CollectionUtils.isEmpty(goodsList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories=parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands=parseBrandAgg(aggs.get(brandAggName));
        //规格参数聚合
        List<Map<String,Object>> specs=null;
        //分类存在并且唯一的情况下聚合
        if (categories != null && categories.size() == 1) {
            specs=buildSpecsificationAgg(categories.get(0).getId(),basicQuery);
        }
        return new SearchResult(totals,totalPages,goodsList,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //创建boolean查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //查询
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        //过滤
        Map<String, String> map = request.getFilter();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            //查询过滤的key
            String key = entry.getKey();
            //处理key
            if(!"cid2".equals(key) && !"brandId".equals(key)){
                key="specs."+key+".keyword";
            }
            //查询过滤的value
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecsificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs=new ArrayList<>();
        //查询指定的规格参数
        List<SpecParam> specParams = specificationClient.querySpecParam(null, cid, true);
        //聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //在原条件的基础上聚合
        queryBuilder.withQuery(basicQuery);
        //聚合开始，将规格参数进行遍历
        for (SpecParam spec : specParams) {
            String name=spec.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //获取聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        //首先获取所有聚合
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : specParams) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //准备Map
            Map<String,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options",terms.getBuckets()
                    .stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }

    //解析商品品牌
    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        }catch (Exception e){
            log.error("[搜索服务]解析品牌异常！",e);
            return null;
        }
    }
    //解析商品分类
    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryListByListIds(ids);
            return categories;
        }catch (Exception e){
            log.error("[搜索服务]解析分类异常！",e);
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        repository.save(goods);
    }
    public void deleteIndex(Long spuId) {
        repository.deleteById(spuId);
    }
}
