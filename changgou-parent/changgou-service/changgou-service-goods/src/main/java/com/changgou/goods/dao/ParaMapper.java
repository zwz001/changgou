package com.changgou.goods.dao;
import com.changgou.goods.pojo.Para;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:admin
 * @Description:Para的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface ParaMapper extends Mapper<Para> {

    @Select("SELECT tbp.* FROM tb_category tbc,tb_para tbp WHERE tbc.`template_id`=tbp.`template_id` AND tbc.`id`=#{id}")
    List<Para> findByCategoryId(Integer id);
}
