package com.recruit.c360.service.collector;
import com.recruit.c360.config.ExternalApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;
import java.util.Map;
@Slf4j @Service @RequiredArgsConstructor
public class WebSearchCollector {
    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties props;

    public Map<String, Object> search(String query) {
        String apiKey = props.getSerper().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Serper API key not configured – web search skipped");
            return Map.of("results", List.of());
        }
        try {
            WebClient client = webClientBuilder.baseUrl(props.getSerper().getBaseUrl()).build();
            @SuppressWarnings("unchecked")
            Map<String,Object> result = client.post()
                .uri("/search")
                .header("X-API-KEY", apiKey)
                .header("Content-Type","application/json")
                .bodyValue(Map.of("q", query, "num", 10))
                .retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> { log.warn("Web search error: {}", e.getMessage()); return Mono.empty(); })
                .block();
            return result != null ? result : Map.of("results", List.of());
        } catch (Exception e) {
            log.error("Web search error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
