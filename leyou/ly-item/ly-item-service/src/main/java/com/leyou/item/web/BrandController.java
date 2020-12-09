package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import com.sun.org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌
 */
@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 品牌数据查询
     * @param page
     * @param rows
     * @param desc
     * @param sortBy
     * @param search
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> queryBrandListByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "desc",defaultValue = "false") boolean desc,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "search",required = false) String search
    ){
        PageResult<Brand> brandPageResult=brandService.queryBrandListByPage(page,rows,desc,sortBy,search);
        return ResponseEntity.ok(brandPageResult);
    }

    /**
     *品牌数据增加
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam("cids") List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 编辑页面根据id查询
     * @param bid
     * @return
     */
    @GetMapping("/bid")
    public ResponseEntity <List<Brand>> queryBrandById(@RequestParam("bid") Long bid){
        brandService.queryBrandById(bid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 编辑品牌
     * @param brand
     * @param cids
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand,@RequestParam("cids") List<Long> cids){
           System.err.println(brand.getId()+"-------"+cids);
           brandService.updateBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED ).build();
    }

    /**
     * 删除品牌
     * @param bid
     * @return
     */
    @DeleteMapping("/del/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") Long bid){
           brandService.deleteBrand(bid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid查询品牌
     * @param cid
     * @return
     */
    @GetMapping("/cid/{cid}")
    public ResponseEntity <List<Brand>> queryByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(brandService.queryByCid(cid));
    }

    /**
     *根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryByListCid(@PathVariable("id") Long id){
        return ResponseEntity.ok(brandService.queryByIds(id));
    }
/**
     *
     * @param ids
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
