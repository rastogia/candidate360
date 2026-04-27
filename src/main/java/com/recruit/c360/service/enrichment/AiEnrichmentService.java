package com.recruit.c360.service.enrichment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.c360.config.ExternalApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.*;
@Slf4j @Service @RequiredArgsConstructor
public class AiEnrichmentService {
    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties props;
    private final ObjectMapper objectMapper;

    public Map<String, Object> enrich(String rawDataJson, String candidateName) {
        String geminiKey = props.getGemini().getApiKey();
        if (geminiKey != null && !geminiKey.isBlank()) {
            return callGemini(rawDataJson, candidateName, geminiKey);
        }
        String openaiKey = props.getOpenai().getApiKey();
        if (openaiKey != null && !openaiKey.isBlank()) {
            return callOpenAI(rawDataJson, candidateName, openaiKey);
        }
        log.info("No AI API key configured – returning structured raw data");
        return Map.of("note","No AI key configured","rawDataSummary","See raw_source_data table");
    }

    private Map<String, Object> callGemini(String rawDataJson, String name, String apiKey) {
        try {
            String prompt = buildPrompt(rawDataJson, name);
            Map<String,Object> req = Map.of("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))));
            WebClient client = webClientBuilder.baseUrl(props.getGemini().getBaseUrl()).build();
            @SuppressWarnings("unchecked")
            Map<String,Object> resp = client.post()
                .uri("/v1beta/models/gemini-1.5-flash:generateContent?key={k}", apiKey)
                .bodyValue(req).retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(e -> { log.warn("Gemini error: {}", e.getMessage()); return Mono.empty(); })
                .block();
            if (resp == null) return Map.of("error","gemini_empty");
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> candidates = (List<Map<String,Object>>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return Map.of("error","no_candidates");
            @SuppressWarnings("unchecked")
            Map<String,Object> content = (Map<String,Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> parts = (List<Map<String,Object>>) content.get("parts");
            String text = String.valueOf(parts.get(0).get("text"));
            return parseAiResponse(text);
        } catch (Exception e) {
            log.error("Gemini enrichment error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private Map<String, Object> callOpenAI(String rawDataJson, String name, String apiKey) {
        try {
            String prompt = buildPrompt(rawDataJson, name);
            Map<String,Object> req = Map.of(
                "model","gpt-4o-mini",
                "messages", List.of(
                    Map.of("role","system","content","You are an expert technical recruiter analyst."),
                    Map.of("role","user","content",prompt)),
                "temperature", 0.3);
            WebClient client = webClientBuilder.baseUrl(props.getOpenai().getBaseUrl()).build();
            @SuppressWarnings("unchecked")
            Map<String,Object> resp = client.post()
                .uri("/v1/chat/completions")
                .header("Authorization","Bearer "+apiKey)
                .bodyValue(req).retrieve().bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(e -> { log.warn("OpenAI error: {}", e.getMessage()); return Mono.empty(); })
                .block();
            if (resp == null) return Map.of("error","openai_empty");
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> choices = (List<Map<String,Object>>) resp.get("choices");
            @SuppressWarnings("unchecked")
            Map<String,Object> msg = (Map<String,Object>) choices.get(0).get("message");
            return parseAiResponse(String.valueOf(msg.get("content")));
        } catch (Exception e) {
            log.error("OpenAI enrichment error: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private String buildPrompt(String rawDataJson, String name) {
        return "Analyse the following aggregated data for candidate " + name + " and return ONLY valid JSON with these keys:\n" +
            "technicalSkillsSummary (string), topLanguages (array), openSourceContributions (string), " +
            "communityReputation (string), communicationStyle (string), consistencyScore (int 0-100), " +
            "collaborationIndicators (string), learningAgility (string), potentialRedFlags (array of objects with flagType,description,severity), " +
            "overallSummary (string).\n\nData:\n" + rawDataJson.substring(0, Math.min(rawDataJson.length(), 8000));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAiResponse(String text) {
        try {
            String json = text.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("rawAiResponse", text);
        }
    }
}
