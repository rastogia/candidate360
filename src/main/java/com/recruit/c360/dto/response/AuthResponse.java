package com.recruit.c360.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
}
