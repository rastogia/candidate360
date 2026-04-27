package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="candidate_handles")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CandidateHandle {
    public enum DataSource { GITHUB, LINKEDIN, TWITTER, STACKOVERFLOW, DEVTO, HACKERNEWS, MEDIUM }
    public enum DiscoveryMethod { AUTO_DISCOVERED, MANUAL }
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="candidate_id", nullable=false) private Candidate candidate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private DataSource source;
    @Column(nullable=false) private String handle;
    @Column(name="profile_url") private String profileUrl;
    @Enumerated(EnumType.STRING) @Column(name="discovery_method")
    @Builder.Default private DiscoveryMethod discoveryMethod = DiscoveryMethod.AUTO_DISCOVERED;
    @Builder.Default private boolean confirmed = false;
    @CreationTimestamp private LocalDateTime createdAt;
}
