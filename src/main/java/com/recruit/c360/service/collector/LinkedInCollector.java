package com.recruit.c360.service.collector;
import com.recruit.c360.config.ExternalApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Map;
@Slf4j @Service @RequiredArgsConstructor
public class LinkedInCollector {
    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties props;

    public Map<String, Object> collect(String linkedinUrl) {
        String apiKey = props.getProxycurl().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.info("ProxyCurl API key not configured – LinkedIn collection skipped");
            return Map.of("error","not_configured");
        }
        try {
            WebClient client = webClientBuilder.baseUrl(props.getProxycurl().getBaseUrl()).build();
            @SuppressWarnings("unchecked")
            Map<String,Object> profile = client.get()
                .uri("/linkedin/profile/resolve?linkedin_profile_url={url}", linkedinUrl)
                .header("Authorization","Bearer "+apiKey)
                .retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(e -> { log.warn("LinkedIn error: {}", e.getMessage()); return Mono.empty(); })
                .block();
            return profile != null ? profile : Map.of("error","not_found");
        } catch (Exception e) {
            log.error("LinkedIn collect error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
