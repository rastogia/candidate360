package com.recruit.c360.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.c360.entity.*;
import com.recruit.c360.service.scoring.ScoringService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
class ScoringServiceTest {
    private final ScoringService scoringService = new ScoringService(new ObjectMapper());

    @Test
    void scoreReturnsValidCompositeScore() {
        Profile360 profile = new Profile360();
        Map<String,Object> data = Map.of(
            "topLanguages", List.of("Java","Python","Go"),
            "consistencyScore", 75,
            "communityReputation", "High",
            "learningAgility", "High",
            "communicationStyle", "Clear and concise",
            "openSourceContributions", "Multiple merged PRs on well-known projects");
        RoleWeight tw = new RoleWeight(); tw.setDimension(RoleWeight.ScoreDimension.TECHNICAL); tw.setWeight(60);
        RoleWeight sw = new RoleWeight(); sw.setDimension(RoleWeight.ScoreDimension.SOCIAL);    sw.setWeight(20);
        RoleWeight bw = new RoleWeight(); bw.setDimension(RoleWeight.ScoreDimension.BEHAVIOURAL);bw.setWeight(20);
        ProfileScore score = scoringService.score(profile, data, List.of(tw, sw, bw));
        assertThat(score.getCompositeScore()).isBetween(0, 100);
        assertThat(score.getCompositeLabel()).isNotBlank();
    }
}
