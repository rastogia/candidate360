package com.recruit.c360.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    public enum Role { ADMIN, RECRUITER }
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) @Builder.Default private Role role = Role.RECRUITER;
    @Builder.Default private boolean active = true;
    @CreationTimestamp private LocalDateTime createdAt;
}
