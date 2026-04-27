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
public class StackOverflowCollector {
    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties props;

    public Map<String, Object> collect(String userId) {
        try {
            WebClient client = webClientBuilder.baseUrl(props.getStackoverflow().getBaseUrl()).build();
            String key = props.getStackoverflow().getKey();
            String keyParam = (key != null && !key.isBlank()) ? "&key=" + key : "";

            @SuppressWarnings("unchecked")
            Map<String,Object> user = client.get()
                .uri("/users/{id}?site=stackoverflow"+keyParam, userId)
                .retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> { log.warn("SO user error: {}", e.getMessage()); return Mono.empty(); })
                .block();

            @SuppressWarnings("unchecked")
            Map<String,Object> answers = client.get()
                .uri("/users/{id}/answers?site=stackoverflow&sort=votes&pagesize=20"+keyParam, userId)
                .retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> Mono.empty()).block();

            @SuppressWarnings("unchecked")
            Map<String,Object> badges = client.get()
                .uri("/users/{id}/badges?site=stackoverflow"+keyParam, userId)
                .retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> Mono.empty()).block();

            return Map.of(
                "user",    user    != null ? user    : Map.of(),
                "answers", answers != null ? answers : Map.of(),
                "badges",  badges  != null ? badges  : Map.of());
        } catch (Exception e) {
            log.error("SO collect error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
