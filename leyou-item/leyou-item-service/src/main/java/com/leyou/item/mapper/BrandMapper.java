package com.leyou.item.mapper;


import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

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


    /**
     * 根据分类id查询品牌
     * @param cid
     * @return
     * left join(左联接) 返回包括左表中的所有记录和右表中联结字段相等的记录
     * right join(右联接) 返回包括右表中的所有记录和左表中联结字段相等的记录
     * inner join(等值连接) 只返回两个表中联结字段相等的行
     */
    @Select("SELECT b.* from tb_brand b INNER JOIN tb_category_brand cb on b.id=cb.brand_id where cb.category_id=#{cid}")
    List<Brand> selectBrandByCid(Long cid);
}
