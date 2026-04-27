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
public class GitHubCollector {
    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties props;

    public Map<String, Object> collect(String username) {
        try {
            WebClient client = webClientBuilder.baseUrl(props.getGithub().getBaseUrl()).build();
            String token = props.getGithub().getToken();
            WebClient.RequestHeadersSpec<?> req = client.get().uri("/users/{u}", username);
            if (token != null && !token.isBlank()) req = req.header("Authorization","Bearer "+token);
            @SuppressWarnings("unchecked")
            Map<String,Object> profile = req.retrieve()
                .bodyToMono(Map.class).timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> { log.warn("GitHub profile error: {}", e.getMessage()); return Mono.empty(); })
                .block();
            if (profile == null) return Map.of("error","not_found");

            @SuppressWarnings("unchecked")
            java.util.List<Map<String,Object>> repos = client.get()
                .uri("/users/{u}/repos?sort=pushed&per_page=30", username)
                .header("Authorization", token != null && !token.isBlank() ? "Bearer "+token : "")
                .retrieve().bodyToFlux(Map.class)
                .timeout(Duration.ofSeconds(15)).collectList()
                .onErrorResume(e -> Mono.just(java.util.List.of())).block();

            return Map.of("profile", profile, "repos", repos != null ? repos : java.util.List.of());
        } catch (Exception e) {
            log.error("GitHub collect error for {}: {}", username, e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
