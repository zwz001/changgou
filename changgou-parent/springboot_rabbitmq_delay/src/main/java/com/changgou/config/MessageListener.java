package com.changgou.config;

import com.changgou.config.QueueConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

//用于接收消息
@Component
@RabbitListener(queues = QueueConfig.QUEUE_MESSAGE)
public class MessageListener {

    /**
     * 监听消息
     * @param msg
     */
    @RabbitHandler
    public void msg(@Payload Object msg){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("当前时间："+dateFormat.format(new Date()));
        System.out.println("收到信息："+msg);
    }
    /***
     * 监听消息
     * @param msg
     *//*
    @RabbitHandler
    public void msg(@Payload Object msg){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("当前时间:"+dateFormat.format(new Date()));
        System.out.println("收到信息:"+msg);
    }*/
}
