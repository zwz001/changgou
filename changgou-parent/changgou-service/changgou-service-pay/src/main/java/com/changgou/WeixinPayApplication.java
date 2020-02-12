package com.changgou;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.management.Query;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
@EnableFeignClients
public class WeixinPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class,args);
    }

    @Autowired
    public Environment environment;
    /**
     * 创建DirectExchange交换机
     * @return
     */
    @Bean
    public DirectExchange basicExchange(){
        return new DirectExchange(environment.getProperty("mq.pay.exchange.order"),true,false);
    }

    /**
     * 创建队列
     * @return
     */
    @Bean(name = "queueOrder")
    public Queue queueOrder(){
        return new Queue(environment.getProperty("mq.pay.queue.order"),true);
    }

    /**
     * 队列绑定到交换机上
     * @return
     */
    @Bean
    public Binding basicBinding(){
        return BindingBuilder.bind(queueOrder()).to(basicExchange()).with(environment.getProperty("mq.pay.routing.key"));
    }

    /**
     * 创建队列
     * @return
     */
    @Bean
    public Queue createSeckillQueue(){
        //queue.order
        return new Queue(environment.getProperty("mq.pay.queue.seckillorder"));
    }

    /**
     * 创建交换机
     * @return
     */
    @Bean
    public DirectExchange createSeckillExchange(){
        return new DirectExchange(environment.getProperty("mq.pay.exchange.seckillorder"));
    }

    /**
     * 绑定队列到交换机
     * @return
     */
    @Bean
    public Binding seckillbinding(){
        //routing key:queue.order
        String property=environment.getProperty("mq.pay.routing.seckillkey");
        return BindingBuilder.bind(createSeckillQueue()).to(createSeckillExchange()).with(property);
    }

}
