package com.leyou.item.mapper;

import com.leyou.common.maapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    @Select("SELECT b.* FROM tb_brand b INNER JOIN tb_category_brand cb ON b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    public List<Brand> queryByCid(@Param("cid") Long cid);
    @Insert("INSERT INTO tb_category_brand (category_id ,brand_id) VALUES (#{cid},#{bid})")
    public int categoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);
    @Update("UPDATE tb_category_brand SET category_id = #{cid} , brand_id = #{bid} WHERE brand_id = #{bid}")
    public int updateBrand(@Param("cid") Long cid, @Param("bid") Long bid);
    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{bid}")
    public int deleteBrand(@Param("bid") Long bid);
}
