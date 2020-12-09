package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecParam> querySpecParam(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> paramList = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(paramList)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_ERROR);
        }
        return paramList;
    }
    @Transactional
    public void saveSpecParam(SpecParam param) {
        param.setId(null);
        int insert = specParamMapper.insertSelective(param);
        if(insert!=1){
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
    }
    @Transactional
    public void updateSpecParam(SpecParam param) {
        SpecParam sp=new SpecParam();
        sp.setId(param.getId());
        List<SpecParam> select = specParamMapper.select(sp);
        if(CollectionUtils.isEmpty(select)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_ERROR);
        }
        int insert = specParamMapper.updateByPrimaryKey(param);
        if(insert!=1){
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
    }
    @Transactional
    public void deleteSpecParam(Long id) {
        int i = specParamMapper.deleteByPrimaryKey(id);
        if(i!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_FOUND);
        }
    }


    public List<SpecGroup> querySpecGroup(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroupList = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(specGroupList)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_FOUND);
        }
        return specGroupList;
    }
    @Transactional
    public void groupUpdate(SpecGroup specGroup) {
        SpecGroup sg = new SpecGroup();
        sg.setId(specGroup.getId());
        List<SpecGroup> groups = specGroupMapper.select(sg);
        if (CollectionUtils.isEmpty(groups)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_FOUND);
        }
        int update = specGroupMapper.updateByPrimaryKey(specGroup);
        if (update != 1) {
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
    }
    @Transactional
    public void groupSave(SpecGroup group) {
        group.setId(null);
        int insert = specGroupMapper.insert(group);
        if (insert != 1) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_ERROR);
        }
    }
    @Transactional
    public void groupDelete(Long id) {
        SpecGroup sg=new SpecGroup();
        sg.setId(id);
        int delete = specGroupMapper.delete(sg);
        if (delete!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_FOUND);
        }
    }

    /**
     * 查询规格组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryListGroupById(Long cid) {
        //查询规格组
        List<SpecGroup> specGroupList = querySpecGroup(cid);
        //查询当前下的分类参数
        List<SpecParam> specParams = querySpecParam(null, cid, null);
        //先把商品的规格参数变成map，key是规格id，map的值是组下的所有参数
        Map<Long,List<SpecParam>> mapParams=new HashMap<>();
        for (SpecParam param:specParams) {
            if (!mapParams.containsKey(param.getGroupId())) {
                //这个组id不存在，则新增一个list
                mapParams.put(param.getGroupId(),new ArrayList<>());
            }
            //存在
            mapParams.get(param.getGroupId()).add(param);
        }
        //填充param到group中
        for (SpecGroup group:specGroupList) {
            group.setSpecParamList(mapParams.get(group.getId()));
        }
        return specGroupList;
    }
}
