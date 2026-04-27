package com.recruit.c360.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
@Configuration @EnableAsync
public class AsyncConfig {
    @Bean(name="profileExecutor")
    public Executor profileExecutor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(4); e.setMaxPoolSize(10);
        e.setQueueCapacity(50); e.setThreadNamePrefix("profile-");
        e.initialize(); return e;
    }
}
