package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wyk
 * @date 2020/7/22 15:34
 */

@Service
public class SpecificationService {


    @Autowired
    private SpecGroupMapper groupMapper;

    @Autowired
    private SpecParamMapper paramMapper;

    /**
     * 根据分类id查询参数组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return this.groupMapper.select(record);
    }


    /**
     * 根据条件查询规格参数
     * @param gid
     * @return
     */
    public List<SpecParam> queryParams(Long gid,Long cid,Boolean generic,Boolean searching) {
        SpecParam record=new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.paramMapper.select(record);
    }

    /**
     * 根据规格参数组查询对应的规格参数
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = this.queryGroupByCid(cid);
        groups.forEach(group -> {
            List<SpecParam> params = this.queryParams(group.getId(), null, null, null);
            group.setParams(params);
        });
        return groups;
    }
}