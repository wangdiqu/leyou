package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * spu查询
 */
public interface GoodsApi {
    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id")Long id);
     /**
     * 根据spuId查询Detail（spu详情）
     * @param sid
     * @return
     */
    @GetMapping("/spu/detail/{sid}")
    SpuDetail queryDetailById(@PathVariable("sid") Long sid);

    /**
     * 根据id查询spu下的所有的sku
     * @param id
     * @return
     */
    @GetMapping("/sku/list")
    List<Sku> querySkuList(@RequestParam("id") Long id);
    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param saleable
     * @param search
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key",required = false) String search
    );
    /**
     * 根据id集合查询SKU数据
     * @param ids
     */
    @GetMapping("/sku/list/ids")
    List<Sku> querySkuByids(@RequestParam("ids") List<Long> ids);
    /**
     * 减库存操作
     * @param carts
     * @return
     */
    @PostMapping("/stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> carts);
}
