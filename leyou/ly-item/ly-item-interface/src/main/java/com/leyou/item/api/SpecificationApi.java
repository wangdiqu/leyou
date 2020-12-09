package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {
    /**
     * 商品规格、参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/spec/params")
    List<SpecParam> querySpecParam(@RequestParam(value = "gid",required = false) Long gid,
                                   @RequestParam(value = "cid",required = false) Long cid,
                                   @RequestParam(value = "searching",required = false) Boolean searching);

    /**
     * 参数规格数组
     * @param cid
     * @return
     */
    @GetMapping("/spec/group")
    List<SpecGroup> querySpecGroup(@RequestParam("cid")Long cid);

}
