package com.changgou.goods;



import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient      //开启客户端的注册
@MapperScan(basePackages = {"com.changgou.goods.dao"})      //将所有的dao中接口的实现类注册到spring容器中
public class SpringbootGoods {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootGoods.class,args);
    }

    /**
     * 雪花算法，用于生成唯一的标识
     * @return
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }

    /*@Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }*/
}
