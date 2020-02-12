package com.changgou.seckill.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.util.annotation.Nullable;

import java.util.concurrent.Executor;

//启用异步处理
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Nullable
    @Override
    public Executor getAsyncExecutor() {
        //定义线程池
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数
        taskExecutor.setCorePoolSize(20);   //默认是8个
        //线程池最大线程数
        taskExecutor.setMaxPoolSize(40);    //设置线程池最大线程数 如果超过次数，则拒绝执行，该值可以根据业务自行设置
        //线程队列最大线程数
        taskExecutor.setQueueCapacity(10);  //线程队列最大线程数

        //初始化
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
