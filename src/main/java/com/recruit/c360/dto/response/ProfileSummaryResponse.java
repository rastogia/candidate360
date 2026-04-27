package com.recruit.c360.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileSummaryResponse {
    private UUID profileId;
    private String candidateName, jobRoleName, status;
    private Integer compositeScore;
    private String compositeLabel;
    private Integer dataCoveragePct;
    private List<DimensionScore> dimensionScores;
    private List<RedFlagItem> redFlags;
    private LocalDateTime createdAt, refreshedAt;
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DimensionScore {
        private String dimension;
        private Integer score, weightUsed;
        private String label;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RedFlagItem {
        private String flagType, description, severity, source;
    }
}
