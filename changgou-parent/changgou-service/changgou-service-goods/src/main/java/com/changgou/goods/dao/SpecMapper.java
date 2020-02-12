package com.changgou.goods.dao;
import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:admin
 * @Description:Spec的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SpecMapper extends Mapper<Spec> {
    @Select("SELECT tbs.* FROM tb_category tbc,tb_spec tbs WHERE tbc.`template_id`=tbs.`template_id` AND tbc.`id`=#{categoryId}")
    List<Spec> findByCategoryId(Integer categoryId);
}
