package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity @Table(name="role_weights")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleWeight {
    public enum ScoreDimension { TECHNICAL, SOCIAL, BEHAVIOURAL }
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="job_role_id", nullable=false) private JobRole jobRole;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ScoreDimension dimension;
    @Column(nullable=false) private int weight;
}
