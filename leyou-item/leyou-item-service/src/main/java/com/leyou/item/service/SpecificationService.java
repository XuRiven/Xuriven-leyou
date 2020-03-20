package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParmaMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParmaMapper specParmaMapper;

    /**
     * 根据分类id查询参数组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return this.specGroupMapper.select(record);
    }

    /**
     * 根据条件查询规格参数
     *
     * @param cid
     * @param gid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record=new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.specParmaMapper.select(record);
    }

    /**
     * 添加规格参数分组
     * @param specGroup
     * @return
     */
    public void addGroups(SpecGroup specGroup) {
        this.specGroupMapper.insert(specGroup);
    }

    /**
     * 修改规格参数分组
     * @param specGroup
     * @return
     */
    public void updateGroup(SpecGroup specGroup) {
        this.specGroupMapper.updateByPrimaryKeySelective(specGroup);
    }

    /**
     * 根据id删除规格参数分组
     * @param id
     * @return
     */
    public void deleteGroupById(Long id) {
        this.specGroupMapper.deleteByPrimaryKey(id);
    }

    /**
     * 添加规格参数
     * @param specParam
     * @return
     */
    public void addParams(SpecParam specParam) {
        this.specParmaMapper.insert(specParam);
    }

    /**
     * 修改规格参数
     * @param specParam
     * @return
     */
    public void updateParams(SpecParam specParam) {
        this.specParmaMapper.updateByPrimaryKeySelective(specParam);
    }


    /**
     * 根据id删除规格参数
     * @param id
     * @return
     */
    public void deleteParam(Long id) {
        this.specParmaMapper.deleteByPrimaryKey(id);
    }
}
