package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Entity @Table(name="job_roles")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class JobRole {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String name;
    @Column(columnDefinition="TEXT") private String description;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by") private User createdBy;
    @Builder.Default private boolean active = true;
    @OneToMany(mappedBy="jobRole", cascade=CascadeType.ALL, orphanRemoval=true) private List<RoleWeight> weights;
    @CreationTimestamp private LocalDateTime createdAt;
}
