package com.recruit.c360.controller;
import com.recruit.c360.dto.request.*;
import com.recruit.c360.dto.response.*;
import com.recruit.c360.entity.*;
import com.recruit.c360.repository.UserRepository;
import com.recruit.c360.service.ProfileOrchestrationService;
import com.recruit.c360.service.report.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/profiles") @RequiredArgsConstructor
@Tag(name="Profiles", description="360 profile orchestration")
@SecurityRequirement(name="bearerAuth")
public class ProfileController {
    private final ProfileOrchestrationService orchestrationService;
    private final PdfReportService pdfReportService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary="Initiate a 360 profile build")
    public ResponseEntity<ApiResponse<String>> build(@Valid @RequestBody BuildProfileRequest req) {
        Profile360 profile = orchestrationService.initiateProfile(req.getCandidateId(), req.getJobRoleId());
        orchestrationService.buildProfile(profile.getId());
        return ResponseEntity.accepted().body(ApiResponse.ok("Profile build started", profile.getId().toString()));
    }

    @GetMapping("/{id}")
    @Operation(summary="Get profile summary")
    public ResponseEntity<ApiResponse<ProfileSummaryResponse>> getSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orchestrationService.getSummary(id)));
    }

    @GetMapping("/{id}/report")
    @Operation(summary="Download PDF report")
    public ResponseEntity<byte[]> downloadReport(@PathVariable UUID id) {
        byte[] pdf = pdfReportService.generateReport(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=profile-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @PostMapping("/{id}/notes")
    @Operation(summary="Add recruiter note")
    public ResponseEntity<ApiResponse<String>> addNote(
            @PathVariable UUID id, @Valid @RequestBody AddNoteRequest req, Authentication auth) {
        User recruiter = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new com.recruit.c360.exception.ResourceNotFoundException("User not found"));
        orchestrationService.addNote(id, req.getNoteText(), recruiter);
        return ResponseEntity.ok(ApiResponse.ok("Note added", null));
    }
}
