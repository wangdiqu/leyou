package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import com.sun.org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi {
    /**
     * 查询品牌
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    Brand queryByListCid(@PathVariable("id") Long id);

    /**
     *
     * @param ids
     * @return
     */
    @GetMapping("/brand/list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);

}
