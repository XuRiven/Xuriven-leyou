package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据查询条件分页查询品牌信息
     *
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化Example
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //key:根据name模糊查询，或者根据首字母查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }
        //page,rows:添加分页条件
        PageHelper.startPage(page, rows);
        //sortBy,desc:添加排序条件
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装成pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //包装成分页结果集返回
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 保存商品信息
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //先新增brand
        this.brandMapper.insertSelective(brand);
        //在新增中间表
        cids.forEach(cid->{
            this.brandMapper.insertBrandAndCategory(cid,brand.getId());
        });

    }

    /**
     * 修改商品信息
     * @param brand
     * @param cids
     */
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        //先修改brand
        this.brandMapper.updateByPrimaryKeySelective(brand);
        //在修改中间表(先删除再新增)
        this.brandMapper.deleteBrandAndCategory(brand.getId());
        cids.forEach(cid->{
            this.brandMapper.insertBrandAndCategory(cid,brand.getId());
        });
    }


    public void deleteBrand(Long bid) {
        if (bid!=null){
            this.brandMapper.deleteByPrimaryKey(bid);
        }
    }
}
