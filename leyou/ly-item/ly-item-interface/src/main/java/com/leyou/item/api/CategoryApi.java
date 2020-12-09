package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    /**
     * 查询分类
     * @param ids
     * @return
     */
    @GetMapping("/category/list/ids")
    List<Category> queryCategoryListByListIds(@RequestParam("ids") List<Long> ids);
}
