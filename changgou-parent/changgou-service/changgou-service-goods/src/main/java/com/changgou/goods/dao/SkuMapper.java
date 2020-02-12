package com.changgou.goods.dao;

import com.changgou.goods.pojo.Sku;
import com.changgou.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:admin
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {

    @Update("UPDATE tb_sku SET num=num-#{num},sale_num=sale_num+#{num} WHERE id=#{skuId} AND num>=#{num}")
    int decrCount(OrderItem orderItem);
}
