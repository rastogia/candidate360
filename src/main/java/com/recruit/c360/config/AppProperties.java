package com.recruit.c360.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data @Component @ConfigurationProperties(prefix="app.jwt")
public class AppProperties {
    private String secret;
    private long expirationMs = 86400000;
}
