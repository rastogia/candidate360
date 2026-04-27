package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="profile_scores")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileScore {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="profile_id", nullable=false) private Profile360 profile;
    private String dimension;
    private Integer score;
    private String label;
    @Column(name="weight_used") private Integer weightUsed;
    @Column(name="composite_score") private Integer compositeScore;
    @Column(name="composite_label") private String compositeLabel;
    @Column(name="data_coverage_pct") private Integer dataCoveragePct;
    @Column(name="sources_available") private String sourcesAvailable;
    @Column(name="sources_missing") private String sourcesMissing;
    @Builder.Default @Column(name="scored_at") private LocalDateTime scoredAt = LocalDateTime.now();
}
