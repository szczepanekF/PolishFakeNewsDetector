package com.pfnd.BusinessLogicService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncExecutorConfig {

    @Bean(name = "factCheckExecutor")
    public Executor factCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // Number of threads to keep in pool
        executor.setMaxPoolSize(20);       // Maximum allowed threads
        executor.setQueueCapacity(100);    // Queue size before spawning new threads
        executor.setThreadNamePrefix("FactCheck-");
        executor.initialize();
        return executor;
    }
}
