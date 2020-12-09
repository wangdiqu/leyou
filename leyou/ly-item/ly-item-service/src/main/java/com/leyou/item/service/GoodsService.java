package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SpecIfCationsMapper specIfCationsMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String search) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //判断搜索内容是否为空
        if (StringUtils.isNotBlank(search)) {
            criteria.andLike("title", "%" + search + "%");
        }
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        //查询数据
        List<Spu> spuList = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        //解析商品名称和分类
        loadCategoryAndBrandName(spuList);
        //解析分页
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spuList);
        return new PageResult<>(spuPageInfo.getTotal(), spuList);
    }

    private void loadCategoryAndBrandName(List<Spu> spuList) {
        for (Spu spu : spuList) {
            //处理分类名称     c->c.getName()==Category::getName
            //将流变成name集合
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));//将names集合转换成字符串用/分开
            //处理品牌名称
            Brand brand = brandService.queryByIds(spu.getBrandId());
            spu.setBname(brand.getName());
        }
    }

    @Transactional
    public void insertSpu(Spu spu) {
        //spu表
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int spuCont = spuMapper.insert(spu);
        if (spuCont != 1) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
        //spuDetail表
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetail.setSpecTemplate(spu.getSpuDetail().getSpecTemplate());
        SpecIfCations specIfCations = specIfCationsMapper.selectByPrimaryKey(spu.getCid3());
        spuDetail.setSpecifications(specIfCations.getSpecifications());
        int spuDetailCont = spuDetailMapper.insert(spuDetail);
        if (spuDetailCont != 1) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
        //新增sku和stock
        this.saveSpu(spu);
        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    /**
     * sku和stock
     * @param spu
     */
    public void saveSpu(Spu spu) {
        int cont;
        //sku表
        List<Stock> stockList = new ArrayList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            cont = skuMapper.insert(sku);
            if (cont != 1) {
                throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
            }
            //stock表
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //批量新增库存
        cont = stockMapper.insertList(stockList);
        if (cont != stockList.size()) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
    }

    public SpuDetail queryById(Long sid) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(sid);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_SPU_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuList(Long sid) {
        Sku sku = new Sku();
        sku.setSpuId(sid);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
        List<Long> collect = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(collect,skuList);
        return skuList;
    }

    @Transactional
    public void updateSpu(Spu spu) {
        if (spu.getId() == null) {
            throw new LyException(ExceptionEnum.GOODS_ID_NOT_NULL);
        }
        //查询数据是否存在
        Sku sku1 = new Sku();
        sku1.setSpuId(spu.getId());
        List<Sku> skuSelect = skuMapper.select(sku1);
        if (!CollectionUtils.isEmpty(skuSelect)) {
            //删除sku
            skuMapper.delete(sku1);
            //删除stock
            List<Long> sidList = skuSelect.stream().map(Sku :: getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(sidList);
        }
        //修改spu表
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        int cont = spuMapper.updateByPrimaryKeySelective(spu);
        if (cont != 1) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //修改spuDetail表
        cont = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (cont != 1) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //调用添加sku和stock方法
        this.saveSpu(spu);
        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }
    @Transactional
    public void deleteSpu(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        int deleteSpu = spuMapper.deleteByPrimaryKey(id);
        if(deleteSpu!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_FOUND);
        }
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        int deleteDetail = spuDetailMapper.deleteByPrimaryKey(id);
        if(deleteDetail!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_FOUND);
        }
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(querySkuList(id));
        //查询spu的详情
        spu.setSpuDetail(queryById(id));
        return spu;
    }

    public List<Sku> querySkuByids(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
        loadStockInSku(ids,skus);
        return skus;
    }
    //查询库存
    private void loadStockInSku(List<Long> ids,List<Sku> skus){
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //把stocks变成一个Map,其key是sku的id值是库存值
        Map<Long, Integer> stockMap = stocks.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        //减库存操作
        //首先遍历carts
        for (CartDTO cart: carts) {
            //减库存操作，为线程安全，采用乐观锁，不采用悲观锁(悲观锁不安全，也会锁住数据库表)
            int stockCount = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (stockCount != 1) {
                throw new LyException(ExceptionEnum.GOODS_UNDER_STOCK);
            }

        }
    }
}
