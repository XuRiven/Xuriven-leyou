package com.leyou.item.mapper;


import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface BrandMapper extends Mapper<Brand> {
    /**
     * 保存商品信息
     * @param cid
     * @param id
     */
    @Insert("INSERT INTO tb_category_brand(category_id,brand_id) VALUES (#{cid},#{bid})")
    void insertBrandAndCategory(@Param("cid") Long cid,@Param("bid") Long id);

    /**
     * 根据商品id删除分类
     * @param id
     */
    @Delete("DELETE FROM `tb_category_brand` WHERE brand_id=#{bid}")
    void deleteBrandAndCategory(@Param("bid") Long id);
}
