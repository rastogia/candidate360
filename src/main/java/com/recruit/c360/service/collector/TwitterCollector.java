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
public class TwitterCollector {
    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties props;

    public Map<String, Object> collect(String username) {
        String bearer = props.getTwitter().getBearerToken();
        if (bearer == null || bearer.isBlank()) {
            log.info("Twitter bearer token not configured");
            return Map.of("error","not_configured");
        }
        try {
            WebClient client = webClientBuilder.baseUrl(props.getTwitter().getBaseUrl()).build();
            @SuppressWarnings("unchecked")
            Map<String,Object> user = client.get()
                .uri("/users/by/username/{u}?user.fields=public_metrics,description,created_at,location", username)
                .header("Authorization","Bearer "+bearer)
                .retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> { log.warn("Twitter error: {}", e.getMessage()); return Mono.empty(); })
                .block();
            return user != null ? user : Map.of("error","not_found");
        } catch (Exception e) {
            log.error("Twitter collect error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
