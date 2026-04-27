package com.recruit.c360.controller;
import com.recruit.c360.dto.request.*;
import com.recruit.c360.dto.response.*;
import com.recruit.c360.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@RestController @RequestMapping("/api/candidates") @RequiredArgsConstructor
@Tag(name="Candidates", description="Candidate management")
@SecurityRequirement(name="bearerAuth")
public class CandidateController {
    private final CandidateService candidateService;

    @PostMapping
    @Operation(summary="Create a new candidate")
    public ResponseEntity<ApiResponse<CandidateResponse>> create(@Valid @RequestBody CreateCandidateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(candidateService.createCandidate(req)));
    }

    @PostMapping("/{id}/cv")
    @Operation(summary="Upload CV for a candidate")
    public ResponseEntity<ApiResponse<CandidateResponse>> uploadCv(
            @PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("CV uploaded and parsed", candidateService.uploadCv(id, file)));
    }

    @PostMapping("/{id}/handles")
    @Operation(summary="Add a social handle manually")
    public ResponseEntity<ApiResponse<CandidateResponse>> addHandle(
            @PathVariable UUID id, @Valid @RequestBody AddHandleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(candidateService.addHandle(id, req)));
    }

    @GetMapping("/{id}")
    @Operation(summary="Get candidate details")
    public ResponseEntity<ApiResponse<CandidateResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(candidateService.getCandidate(id)));
    }

    @GetMapping("/search")
    @Operation(summary="Search candidates by name")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> search(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok(candidateService.searchCandidates(name)));
    }
}
