package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import entity.SystemConstants;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment environment;
    /**
     * 多线程下单操作
     */
    @Async      //标识为异步方法
    public void createOrder(){
        System.out.println("模拟下单===start==="+Thread.currentThread().getName());
        try {
            System.out.println("准备执行...");
            Thread.sleep(20000);
            System.out.println("开始执行...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //从队列中取出元素类型seckillStatus
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps(SystemConstants.SEC_KILL_USER_QUEUE_KEY).rightPop();
        if (seckillStatus != null) {
            Long id = seckillStatus.getGoodsId();
            String time = seckillStatus.getTime();
            String username = seckillStatus.getUsername();

            //1.根据id从redis获取商品的数据
            SeckillGoods seckillGoods= (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).get(id);
            //2.判断是否有库存 或者是否商品存在，如果商品不存在 或者库存为0 则抛出异常
            /*if(seckillGoods==null || seckillGoods.getStockCount()<=0){
                throw new RuntimeException("售罄了");
            }*/
            //3.减少库存
            //seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
            //4.保存到redis中
            //redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).put(id,seckillGoods);
            //5.判断是否库存为0，如果为0，数据更新到数据库中 删除掉redis中的商品
            if (seckillGoods.getStockCount()==0) {
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).delete(id);
            }
            //6.下预订单到redis中 key: username value:订单的数据 某一个用户的订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());      //主键
            seckillOrder.setSeckillId(id);  //购买的商品的id
            seckillOrder.setMoney(seckillGoods.getCostPrice()); //金额
            seckillOrder.setUserId(username);   //订单所属的用户
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).put(username,seckillOrder);

            //下单成功 需要修改当前的用户排队抢单的状态
            seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setStatus(2); //设置成下单成功 等待支付状态

            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).put(username,seckillStatus);
        }
        sendTimerMessage(seckillStatus);    //发送延时消息到MQ中
        System.out.println("模拟下单===end==="+Thread.currentThread().getName());
    }

    /**
     * 发送延时消息到RabbitMq中
     * @param seckillStatus
     */
    public void sendTimerMessage(SeckillStatus seckillStatus){
        rabbitTemplate.convertAndSend(environment.getProperty("mq.pay.queue.seckillordertimerdelay"),
                (Object)JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        message.getMessageProperties().setExpiration("10000");
                        return message;
                    }
                });
    }
}
