package com.changgou.order.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

//监听某一个队列
@RabbitListener(queues = "queue.order")
@Component
public class OrderPayMessageListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler     //由被修饰的方法来处理消息
    public void handlerMsg(String msg){
        //1.接受消息（map对象的JSON字符串）
        Map<String,String> map = JSON.parseObject(msg, Map.class);
        if (map != null) {
            if(map.get("return_code").equals("SUCCESS")){
                if(map.get("result_code").equals("SUCCESS")){   //支付成功
                    //2.更新订单的状态
                    String out_trade_no = map.get("out_trade_no");
                    String time_end = map.get("time_end");
                    String transaction_id = map.get("transaction_id");
                    orderService.updateStatus(out_trade_no,time_end,transaction_id);
                }else {
                    //支付失败删除订单
                    orderService.deleteOrder(map.get("out_trade_no"));
                }
            }
        }
    }
}
