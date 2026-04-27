package com.recruit.c360.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class CreateCandidateRequest {
    @NotBlank private String name;
    private String email, phone, location;
}
