package com.recruit.c360.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data @Component @ConfigurationProperties(prefix="storage")
public class StorageProperties {
    private String cvUploadDir = "uploads/cv";
    private String reportDir   = "uploads/reports";
}
