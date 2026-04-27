package com.recruit.c360.dto.request;
import com.recruit.c360.entity.CandidateHandle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class AddHandleRequest {
    @NotNull private CandidateHandle.DataSource source;
    @NotBlank private String handle;
    private String profileUrl;
}
