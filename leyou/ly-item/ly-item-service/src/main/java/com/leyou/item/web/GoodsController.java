package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * spu
 * sku
 */
@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * SPU
     * @param page
     * @param rows
     * @param saleable
     * @param search
     * @return
     */
    @GetMapping("/spu/page")
    private ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key",required = false) String search
    ){
        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,search));
    }
    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }
    /**
     * spu详情
     * @param sid
     * @return
     */
    @GetMapping("/spu/detail/{sid}")
    public ResponseEntity<SpuDetail> queryById(@PathVariable("sid") Long sid){
        return ResponseEntity.ok(goodsService.queryById(sid));
    }
    @PostMapping("/goods")
    public ResponseEntity<Void> insertSpu(@RequestBody Spu spu){
        goodsService.insertSpu(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("/goods")
    public ResponseEntity<Void> updateSpu(@RequestBody Spu spu){
        goodsService.updateSpu(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/goods/group/{id}")
    public ResponseEntity<Void> deleteSpu(@PathVariable("id") Long id){
        goodsService.deleteSpu(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * SKU
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuList(@RequestParam("id") Long id){
        return ResponseEntity.ok(goodsService.querySkuList(id));
    }
    /**
     * 根据SKU集合查询shu数据
     * @param ids
     * @return
     */
    @GetMapping("/sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByids(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(goodsService.querySkuByids(ids));
    }

    /**
     * 减库存操作
     * @param carts
     * @return
     */
    @PostMapping("/stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
         goodsService.decreaseStock(carts);
         return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
