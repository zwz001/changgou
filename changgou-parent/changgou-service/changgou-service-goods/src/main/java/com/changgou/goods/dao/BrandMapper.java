package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:admin
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {

    @Select("SELECT * FROM tb_brand tbb,tb_category_brand tbcb WHERE tbcb.`brand_id`=tbb.`id` AND tbcb.`category_id`=${value}")
    List<Brand> selectByCategoryId(Integer categoryId);
}
