package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "#{mq.pau.queue.seckillordertimer}")
public class SeckillOrderDelayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitHandler
    public void consumeMessage(@Payload String message){
        //读取消息
        SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);
        //获取Resi中订单信息
        String username = seckillStatus.getUsername();
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
        //如果Redis中有订单信息，说明用户未支付
        if (seckillOrder != null) {
            System.out.println("准备回滚--"+seckillStatus);
            //关闭支付

        }

    }
}
