package com.recruit.c360.service.scoring;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.c360.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
@Slf4j @Service @RequiredArgsConstructor
public class ScoringService {
    private final ObjectMapper objectMapper;

    public ProfileScore score(Profile360 profile, Map<String, Object> enrichedData, List<RoleWeight> weights) {
        int techScore    = scoreTechnical(enrichedData);
        int socialScore  = scoreSocial(enrichedData);
        int behavScore   = scoreBehavioural(enrichedData);
        int coverage     = computeCoverage(enrichedData);

        int composite = 0; int totalWeight = 0;
        for (RoleWeight rw : weights) {
            int dimScore = switch (rw.getDimension()) {
                case TECHNICAL   -> techScore;
                case SOCIAL      -> socialScore;
                case BEHAVIOURAL -> behavScore;
            };
            composite   += dimScore * rw.getWeight();
            totalWeight += rw.getWeight();
        }
        int finalScore = totalWeight > 0 ? composite / totalWeight : 0;

        return ProfileScore.builder()
            .profile(profile)
            .dimension("COMPOSITE")
            .score(finalScore)
            .label(scoreLabel(finalScore))
            .compositeScore(finalScore)
            .compositeLabel(scoreLabel(finalScore))
            .dataCoveragePct(coverage)
            .sourcesAvailable(String.join(",", getAvailableSources(enrichedData)))
            .build();
    }

    private int scoreTechnical(Map<String, Object> data) {
        int score = 40;
        Object skills = data.get("topLanguages");
        if (skills instanceof java.util.List<?> l && !l.isEmpty()) score += Math.min(20, l.size() * 4);
        Object contributions = data.get("openSourceContributions");
        if (contributions instanceof String s && s.length() > 20) score += 15;
        Object consistency = data.get("consistencyScore");
        if (consistency instanceof Integer i) score += i / 10;
        return Math.min(100, score);
    }

    private int scoreSocial(Map<String, Object> data) {
        int score = 40;
        Object reputation = data.get("communityReputation");
        if (reputation instanceof String s && s.toLowerCase().contains("high")) score += 25;
        else if (reputation instanceof String s && s.toLowerCase().contains("medium")) score += 15;
        Object collab = data.get("collaborationIndicators");
        if (collab instanceof String s && s.length() > 20) score += 10;
        return Math.min(100, score);
    }

    private int scoreBehavioural(Map<String, Object> data) {
        int score = 50;
        Object learning = data.get("learningAgility");
        if (learning instanceof String s && s.toLowerCase().contains("high")) score += 20;
        Object comm = data.get("communicationStyle");
        if (comm instanceof String s && s.length() > 10) score += 15;
        Object flags = data.get("potentialRedFlags");
        if (flags instanceof java.util.List<?> l) score -= l.size() * 5;
        return Math.max(0, Math.min(100, score));
    }

    private int computeCoverage(Map<String, Object> data) {
        long present = data.values().stream().filter(v -> v != null && !v.toString().isBlank()).count();
        return (int)(present * 100 / Math.max(1, data.size()));
    }

    private java.util.List<String> getAvailableSources(Map<String, Object> data) {
        java.util.List<String> sources = new ArrayList<>();
        if (data.containsKey("topLanguages")) sources.add("GITHUB");
        if (data.containsKey("communityReputation")) sources.add("STACKOVERFLOW");
        if (data.containsKey("communicationStyle")) sources.add("TWITTER");
        return sources;
    }

    private String scoreLabel(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 65) return "GOOD";
        if (score >= 50) return "AVERAGE";
        if (score >= 35) return "BELOW_AVERAGE";
        return "POOR";
    }
}
