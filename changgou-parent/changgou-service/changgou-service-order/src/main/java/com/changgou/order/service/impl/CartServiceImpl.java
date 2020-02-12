package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;


    /**
     * 添加购物车
     *
     * @param num      购买商品的数量
     * @param id       购买id
     * @param username 购买用户
     */
    @Override
    public void add(Integer num, Long id, String username) {
        //删除该商品的购物车数据
        if(num<=0){
            redisTemplate.boundHashOps("Cart_"+username).delete(id);
            return;
        }
        //查询sku
        Result<Sku> resultSku = skuFeign.findById(id);
        if (resultSku!=null && resultSku.isFlag()){
            //获取sku
            Sku sku = resultSku.getData();
            //获取spu
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //将sku转换成orderItem
            OrderItem orderItem = sku2OrderItem(sku,spu,num);
            /**
             * 购物车数据存入到Redis中
             * namespace=cart_[username]
             * key=id(sku)
             * value=orderItem
             */
            redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);
        }
    }

    private OrderItem sku2OrderItem(Sku sku,Spu spu,Integer num){
        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num*orderItem.getPrice());   //单价*数量
        orderItem.setPayMoney(num*orderItem.getPrice());    //实付金额
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight()*num);           //重量=单个重量*数量

        //分类ID设置
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }

    /**
     * 查询用户的购物车数据
     *
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {

        //查询所有购物车数据
        List<OrderItem> orderItems = redisTemplate.boundHashOps("Cart_" + username).values();
        return orderItems;
    }
}
