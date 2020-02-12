package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.SystemConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillOrderPayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 监听消费消息
     *
     * @param message
     */
    @RabbitHandler
    public void consumeMessage(@Payload String message) {
        System.out.println(message);
        //将消息转换成map对象
        Map<String, String> map = JSON.parseObject(message, Map.class);
        System.out.println("监听到的消息：" + map);
        String out_trade_no = map.get("out_trade_no");
        String attach = map.get("attach");
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
        if (map != null && map.get("return_code").equalsIgnoreCase("SUCCESS")) {
            //支付成功
            //if (map.get("result_code").equalsIgnoreCase("SUCCESS")){
            //2获取订单的id,获取交易流水，获取支付时间
            SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).get(attachMap.get("username"));
            //}
            //3.更新订单
            seckillOrder.setStatus("1");    //已经支付
            String time_end = map.get("time_end");    //支付时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                Date parse = simpleDateFormat.parse(time_end);
                seckillOrder.setPayTime(parse); //设置支付时间
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String transaction_id = map.get("transaction_id");
            seckillOrder.setTransactionId(transaction_id);
            seckillOrderMapper.insertSelective(seckillOrder);

            //清空redis中的订单
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(attachMap.get("username"));

            //删除排队信息
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_QUEUE_REPEAT_KEY).delete(attachMap.get("username"));

            //删除抢单信息
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(attachMap.get("username"));
        } else {
            RLock mylock = redissonClient.getLock("Mylock");
            //支付失败--》删除订单--》恢复库存--》积分恢复
            //调用关闭订单的Api关闭支付订单
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps(
                    SystemConstants.SEC_KILL_USER_STATUS_KEY).get(attachMap.get("username"));
            try {
                //上锁
                mylock.lock(100, TimeUnit.SECONDS);
                //恢复库存
                SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(
                        SystemConstants.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
                if (seckillGoods == null) {
                    seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
                }
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX +
                        seckillStatus.getTime()).put(seckillStatus.getGoodsId(), seckillGoods);

                //清空redis中的订单
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(attachMap.get("username"));

                //删除排队信息
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_QUEUE_REPEAT_KEY).delete(attachMap.get("username"));

                //删除抢单信息
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(attachMap.get("username"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //释放锁
                mylock.unlock();
            }
        }

    }


}
