package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SpringbootEureka {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootEureka.class,args);
    }
}
