package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Entity @Table(name="profiles_360")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile360 {
    public enum ProfileStatus { PENDING, COLLECTING, ENRICHING, SCORING, COMPLETE, FAILED }
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="candidate_id", nullable=false) private Candidate candidate;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="job_role_id", nullable=false) private JobRole jobRole;
    @Enumerated(EnumType.STRING) @Builder.Default private ProfileStatus status = ProfileStatus.PENDING;
    @Column(name="enriched_json", columnDefinition="TEXT") private String enrichedJson;
    @Column(name="refreshed_at") private LocalDateTime refreshedAt;
    @CreationTimestamp private LocalDateTime createdAt;
    @OneToMany(mappedBy="profile", cascade=CascadeType.ALL) private List<ProfileScore> scores;
    @OneToMany(mappedBy="profile", cascade=CascadeType.ALL) private List<RedFlag> redFlags;
}
