package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品参数及分组
 */
@RestController
@RequestMapping("spec")
public class SpecificationController {
   @Autowired
    private SpecificationService specService;


    /**
     * 参数数据查询
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParam(@RequestParam(value = "gid",required = false) Long gid,
                                                          @RequestParam(value = "cid",required = false) Long cid,
                                                          @RequestParam(value = "searching",required = false) Boolean searching) {
        return ResponseEntity.ok(specService.querySpecParam(gid,cid,searching));
    }
    /**
     * 根据分类查询规格组
     * @param cid
     * @return
     */
    @GetMapping("/group")
    public ResponseEntity<List<SpecGroup>> queryListGroupById(@RequestParam("cid")Long cid){
        return ResponseEntity.ok(specService.queryListGroupById(cid));
    }
    @PostMapping("/param")
    public ResponseEntity<Void> saveSpecParam(@RequestBody SpecParam param){
        specService.saveSpecParam(param);
         return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("/param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam param){
        specService.updateSpecParam(param);
         return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/param/{id}")
    public ResponseEntity<Void> updateSpecParam(@PathVariable Long id){
        specService.deleteSpecParam(id);
         return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 规格数据查询
     * @param cid
     * @return
     */
    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroup(@PathVariable("cid") Long cid){
         return ResponseEntity.ok(specService.querySpecGroup(cid));
    }
    @PostMapping("/group")
    public ResponseEntity<Void> groupSave(@RequestBody SpecGroup group){
        specService.groupSave(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/group")
    public ResponseEntity<Void> groupUpdate(@RequestBody SpecGroup group){
        specService.groupUpdate(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/group/{id}")
    public ResponseEntity<Void> groupDelete(@PathVariable("id") Long id){
        specService.groupDelete(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
