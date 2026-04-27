package com.recruit.c360.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CandidateResponse {
    private UUID id;
    private String name, email, phone, location, cvFileName;
    private LocalDateTime createdAt;
    private List<HandleResponse> handles;
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HandleResponse {
        private UUID id;
        private String source, handle, profileUrl, discoveryMethod;
        private boolean confirmed;
    }
}
