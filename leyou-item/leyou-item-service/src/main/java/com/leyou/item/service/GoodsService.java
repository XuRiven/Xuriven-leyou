package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBO;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    /**
     * 根据条件分页查询Spu
     *
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBO> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //添加查询条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }

        //添加上下架的过滤条件
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        //添加分页
        PageHelper.startPage(page, rows);

        //执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);
        //spu集合转化成spubo集合
        List<SpuBO> spuBos = spus.stream().map(spu -> {
            SpuBO spuBo = new SpuBO();
            //查询品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            BeanUtils.copyProperties(spu, spuBo);
            if (brand != null) {
                spuBo.setBname(brand.getName());
                //查询分类名称
                List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
                spuBo.setCname(StringUtils.join(names, "-"));
            }
            return spuBo;
        }).collect(Collectors.toList());

        //返回pageResult<spuBO>
        return new PageResult<>(spuPageInfo.getTotal(), spuBos);
    }


    /**
     * 新增商品
     *
     * @param spuBO
     * @return
     */
    @Transactional
    public void saveGoods(SpuBO spuBO) {
        //先新增spu
        spuBO.setId(null);
        spuBO.setSaleable(true);
        spuBO.setValid(true);
        spuBO.setCreateTime(new Date());
        spuBO.setLastUpdateTime(spuBO.getCreateTime());
        this.spuMapper.insertSelective(spuBO);

        //再去新增spuDetail
        SpuDetail spuDetail = spuBO.getSpuDetail();
        spuDetail.setSpuId(spuBO.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBO);
    }

    private void saveSkuAndStock(SpuBO spuBO) {
        spuBO.getSkus().forEach(sku -> {
            //新增sku
            sku.setId(null);
            sku.setSpuId(spuBO.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            //新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }


    /**
     * 根据spuId查询spuDetail
     *
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }


    /**
     * 根据spuId查询sku集合
     *
     * @param id
     * @return
     */
    public List<Sku> querySkusBySpuId(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }


    /**
     * 更新商品信息
     *
     * @param spuBO
     * @return
     */
    public void updateGoods(SpuBO spuBO) {
        //根据spuId查询要删除的sku
        Sku record = new Sku();
        record.setSpuId(spuBO.getId());
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            //删除stock
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });

        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBO.getId());
        this.skuMapper.delete(sku);

        //新增spu和stock
        this.saveSkuAndStock(spuBO);

        //更新spu和sotck
        spuBO.setCreateTime(null);
        spuBO.setLastUpdateTime(new Date());
        spuBO.setValid(null);
        spuBO.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBO);
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBO.getSpuDetail());

    }
}
