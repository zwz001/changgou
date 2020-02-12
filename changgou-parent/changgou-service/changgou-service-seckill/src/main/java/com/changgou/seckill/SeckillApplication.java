package com.changgou.seckill;

import entity.IdWorker;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@MapperScan(basePackages = {"com.changgou.seckill.dao"})
@EnableScheduling
@EnableAsync
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }

    //spring的注解
    @Autowired
    private Environment environment;

    //创建队列
    @Bean
    public Queue createQueue(){
        //queue.order
        return new Queue(environment.getProperty("mq.pay.queue.order"));
    }

    //创建交换机
    @Bean
    public DirectExchange createExchange(){
        //exchange.order
        return new DirectExchange(environment.getProperty("mq.pay.exchange.order"));
    }

    //绑定队列到交换机
    @Bean
    public Binding binding(){
        //routing key:queue.order
        String property = environment.getProperty("mq.pay.routing.key");
        return BindingBuilder.bind(createQueue()).to(createExchange()).with(property);
    }

    //创建队列
    @Bean
    public Queue createSeckillQueue(){
        return new Queue(environment.getProperty("mq.pay.queue.seckillorder"));
    }

    //创建交换机
    @Bean
    public DirectExchange createSeckillExchange(){
        return new DirectExchange(environment.getProperty("mq.pay.exchange.seckillorder"));
    }

    //绑定队列到交换机
    @Bean
    public Binding seckillBinding(){
        String property = environment.getProperty("mq.pay.routing.seckillkey");
        return BindingBuilder.bind(createQueue()).to(createSeckillExchange()).with(property);
    }

    //到期数据队列
    @Bean
    public Queue seckillOrderTimerQueue(){
        return new Queue(environment.getProperty("mq.pay.queue.seckillordertimer"),true);
    }

    /**
     * 超时数据队列
     * @return
     */
    @Bean
    public Queue delaySeckillOrderTimerQueue(){
        return QueueBuilder.durable(environment.getProperty("mq.pay.queue.seckillordertimerdelay"))
                .withArgument("x-dead-letter-exchange",environment.getProperty(",q/pay.exchange.order"))    //消息进入死信队列
                .withArgument("x-dead-letter-routing-key",environment.getProperty("mq.pay.queue.seckillordertimer"))    //绑定指定的routing-key
                .build();
    }

    /**
     * 将队列绑定到交换机上
     * @return
     */
    @Bean
    public Binding basicBinding(){
        return BindingBuilder.bind(seckillOrderTimerQueue()).to(createExchange())
                .with(environment.getProperty("mq.pay.queue.seckillordertimer"));
    }
}
