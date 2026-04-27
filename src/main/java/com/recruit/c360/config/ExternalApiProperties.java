package com.recruit.c360.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data @Component @ConfigurationProperties(prefix="external")
public class ExternalApiProperties {
    private Api github = new Api();
    private Api stackoverflow = new Api();
    private Api serper = new Api();
    private Api gemini = new Api();
    private Api openai = new Api();
    private Api twitter = new Api();
    private Api proxycurl = new Api();
    @Data public static class Api {
        private String token, key, apiKey, bearerToken, baseUrl;
    }
}
