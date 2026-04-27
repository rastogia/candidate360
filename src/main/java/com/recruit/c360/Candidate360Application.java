package com.recruit.c360;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableConfigurationProperties
public class Candidate360Application {
    public static void main(String[] args) {
        SpringApplication.run(Candidate360Application.class, args);
    }
}
