package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long pid){
        Category t= new Category();
        t.setParentId(pid);
        List<Category> categoryList = categoryMapper.select(t);//select方法会把参数对象中的非空值作为条件
        if (CollectionUtils.isEmpty(categoryList)) {//判断是否为空
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }
    public List<Category> queryByIds(List<Long> ids){
        List<Category> selectByIdList = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(selectByIdList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return selectByIdList;
    }

    public List<Category> queryAllCategoryLevelByCid3(Long id) {
        Category category=new Category();
        category.setId(id);
        List<Category> categories = categoryMapper.select(category);
        return categories;
    }
}
