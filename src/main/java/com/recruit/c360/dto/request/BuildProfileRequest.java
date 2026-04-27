package com.recruit.c360.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;
@Data public class BuildProfileRequest {
    @NotNull private UUID candidateId;
    @NotNull private UUID jobRoleId;
    private boolean forceRefresh = false;
}
