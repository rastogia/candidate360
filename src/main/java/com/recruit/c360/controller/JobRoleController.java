package com.recruit.c360.controller;
import com.recruit.c360.dto.request.CreateJobRoleRequest;
import com.recruit.c360.dto.response.ApiResponse;
import com.recruit.c360.entity.*;
import com.recruit.c360.exception.ResourceNotFoundException;
import com.recruit.c360.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/job-roles") @RequiredArgsConstructor
@Tag(name="Job Roles", description="Job role and scoring weight management")
@SecurityRequirement(name="bearerAuth")
public class JobRoleController {
    private final JobRoleRepository jobRoleRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary="Create job role with scoring weights")
    public ResponseEntity<ApiResponse<JobRole>> create(@Valid @RequestBody CreateJobRoleRequest req,
            org.springframework.security.core.Authentication auth) {
        User creator = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        JobRole role = JobRole.builder().name(req.getName()).description(req.getDescription())
            .createdBy(creator).build();
        jobRoleRepository.save(role);
        if (req.getWeights() != null) {
            java.util.List<RoleWeight> weights = req.getWeights().stream().map(w -> {
                RoleWeight rw = new RoleWeight();
                rw.setJobRole(role);
                rw.setDimension(RoleWeight.ScoreDimension.valueOf(w.getDimension().toUpperCase()));
                rw.setWeight(w.getWeight());
                return rw;
            }).toList();
            role.setWeights(weights);
            jobRoleRepository.save(role);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(role));
    }

    @GetMapping
    @Operation(summary="List all active job roles")
    public ResponseEntity<ApiResponse<List<JobRole>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(jobRoleRepository.findByActiveTrue()));
    }

    @GetMapping("/{id}")
    @Operation(summary="Get job role by id")
    public ResponseEntity<ApiResponse<JobRole>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(jobRoleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("JobRole not found: " + id))));
    }
}
