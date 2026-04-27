package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="recruiter_notes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RecruiterNote {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="profile_id", nullable=false) private Profile360 profile;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="recruiter_id", nullable=false) private User recruiter;
    @Column(name="note_text", nullable=false, columnDefinition="TEXT") private String noteText;
    @CreationTimestamp private LocalDateTime createdAt;
}
