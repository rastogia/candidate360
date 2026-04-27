package com.recruit.c360.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
@Data public class CreateJobRoleRequest {
    @NotBlank private String name;
    private String description;
    private List<WeightEntry> weights;
    @Data public static class WeightEntry {
        private String dimension;
        private int weight;
    }
}
