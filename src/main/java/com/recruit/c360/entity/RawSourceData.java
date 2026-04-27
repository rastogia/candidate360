package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="raw_source_data")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RawSourceData {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="candidate_id", nullable=false) private Candidate candidate;
    @Column(nullable=false) private String source;
    @Column(name="json_payload", columnDefinition="TEXT") private String jsonPayload;
    @Builder.Default @Column(name="fetched_at") private LocalDateTime fetchedAt = LocalDateTime.now();
    @Builder.Default @Column(name="is_stale") private boolean stale = false;
    @Column(name="error_message") private String errorMessage;
}
