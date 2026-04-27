package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="red_flags")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RedFlag {
    public enum Severity { LOW, MEDIUM, HIGH }
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="profile_id") private Profile360 profile;
    @Column(name="flag_type", nullable=false) private String flagType;
    @Column(nullable=false, columnDefinition="TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Severity severity;
    @Column(name="source") private String source;
    @CreationTimestamp private LocalDateTime createdAt;
}
