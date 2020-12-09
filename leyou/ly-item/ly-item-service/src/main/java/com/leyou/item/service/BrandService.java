package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 品牌数据查询
     *
     * @param page
     * @param rows
     * @param desc
     * @param sortBy
     * @param search
     * @return
     */
    public PageResult<Brand> queryBrandListByPage(
            Integer page, Integer rows, boolean desc, String sortBy, String search) {
        //1.分页
        PageHelper.startPage(page, rows);
        //2.过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(search)) {
            example.createCriteria().orLike("name", "%" + search + "%")
                    .orEqualTo("letter", search.toUpperCase());
        }
        //3.排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //4.查询
        List<Brand> brandLists = brandMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(brandLists)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //解析分页结果,判断brandLists中是否包含page并解析
        PageInfo<Brand> info = new PageInfo<>(brandLists);
        return new PageResult(info.getTotal(), brandLists);
    }
    /**
     * 品牌数据添加
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        brand.setId(null);
        int cont = brandMapper.insert(brand);
        if (cont != 1) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
        //新增中间表tb_category_brand数据
        for (Long cid : cids) {
            cont = brandMapper.categoryBrand(cid, brand.getId());
            if (cont != 1) {
                throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
            }
        }
    }
    /**
     * 根据ID查询实体
     *
     * @param bid
     * @return
     */
    public List<Brand> queryBrandById(Long bid) {
        Brand brand = new Brand();
        brand.setId(bid);
        List<Brand> brandList = brandMapper.select(brand);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }

    /**
     * 修改数据
     *
     * @param brand
     */
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        List<Brand> byId = this.queryBrandById(brand.getId());
        if (CollectionUtils.isEmpty(byId)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        brand.setId(brand.getId());
        int cont = brandMapper.updateByPrimaryKey(brand);
        if (cont != 1) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //修改增中间表tb_category_brand数据
         Category category = new Category();
        for (Long cid : cids) {
            category.setId(cid);
            List<Category> categories = categoryMapper.select(category);
            if (CollectionUtils.isEmpty(categories)) {
                throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
            }
            cont = brandMapper.updateBrand(cid, brand.getId());
            if (cont != 1) {
                throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
            }
        }
    }

    /**
     * 根据id删除数据
     *
     * @param bid
     */
    @Transactional
    public void deleteBrand(Long bid) {
        List<Brand> brands = this.queryBrandById(bid);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        Brand brand = new Brand();
        brand.setId(bid);
        int delete = brandMapper.delete(brand);
        if (delete <= 0) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_FOUND);
        }
        //删除中间表
        int deleteBrand = brandMapper.deleteBrand(bid);
        if (deleteBrand <= 0) {
            throw new LyException(ExceptionEnum.BRAND_DELETE_FOUND);
        }
    }
    public Brand queryByIds(Long bid){
        Brand brand = brandMapper.selectByPrimaryKey(bid);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCid(cid);
        if (CollectionUtils.isEmpty(brandList) || brandList.size() <=0) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }
    //解析聚合品牌用
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
