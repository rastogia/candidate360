package com.recruit.c360.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.c360.dto.response.ProfileSummaryResponse;
import com.recruit.c360.entity.*;
import com.recruit.c360.exception.ResourceNotFoundException;
import com.recruit.c360.repository.*;
import com.recruit.c360.service.collector.*;
import com.recruit.c360.service.enrichment.AiEnrichmentService;
import com.recruit.c360.service.scoring.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class ProfileOrchestrationService {
    private final CandidateRepository candidateRepository;
    private final JobRoleRepository jobRoleRepository;
    private final CandidateHandleRepository handleRepository;
    private final RawSourceDataRepository rawSourceDataRepository;
    private final Profile360Repository profileRepository;
    private final ProfileScoreRepository scoreRepository;
    private final RedFlagRepository redFlagRepository;
    private final RecruiterNoteRepository noteRepository;
    private final GitHubCollector gitHubCollector;
    private final StackOverflowCollector stackOverflowCollector;
    private final TwitterCollector twitterCollector;
    private final LinkedInCollector linkedInCollector;
    private final AiEnrichmentService aiEnrichmentService;
    private final ScoringService scoringService;
    private final RedFlagEngine redFlagEngine;
    private final ObjectMapper objectMapper;

    @Transactional
    public Profile360 initiateProfile(UUID candidateId, UUID jobRoleId) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));
        JobRole jobRole = jobRoleRepository.findById(jobRoleId)
            .orElseThrow(() -> new ResourceNotFoundException("JobRole not found: " + jobRoleId));
        Optional<Profile360> existing = profileRepository.findByCandidateIdAndJobRoleId(candidateId, jobRoleId);
        if (existing.isPresent()) return existing.get();
        Profile360 profile = Profile360.builder()
            .candidate(candidate).jobRole(jobRole)
            .status(Profile360.ProfileStatus.PENDING).build();
        return profileRepository.save(profile);
    }

    @Async("profileExecutor")
    @Transactional
    public void buildProfile(UUID profileId) {
        Profile360 profile = profileRepository.findById(profileId).orElse(null);
        if (profile == null) return;
        try {
            profile.setStatus(Profile360.ProfileStatus.COLLECTING);
            profileRepository.save(profile);
            Map<String, Object> collectedData = collectAllSources(profile);

            profile.setStatus(Profile360.ProfileStatus.ENRICHING);
            profileRepository.save(profile);
            String rawJson = objectMapper.writeValueAsString(collectedData);
            Map<String, Object> enriched = aiEnrichmentService.enrich(rawJson, profile.getCandidate().getName());

            profile.setStatus(Profile360.ProfileStatus.SCORING);
            profile.setEnrichedJson(objectMapper.writeValueAsString(enriched));
            profileRepository.save(profile);

            List<RoleWeight> weights = profile.getJobRole().getWeights();
            ProfileScore score = scoringService.score(profile, enriched, weights != null ? weights : List.of());
            scoreRepository.save(score);

            List<RedFlag> flags = redFlagEngine.detect(profile, enriched);
            redFlagRepository.saveAll(flags);

            profile.setStatus(Profile360.ProfileStatus.COMPLETE);
            profile.setRefreshedAt(LocalDateTime.now());
            profileRepository.save(profile);
            log.info("Profile {} built successfully", profileId);
        } catch (Exception e) {
            log.error("Profile build failed for {}: {}", profileId, e.getMessage());
            profile.setStatus(Profile360.ProfileStatus.FAILED);
            profileRepository.save(profile);
        }
    }

    private Map<String, Object> collectAllSources(Profile360 profile) {
        Map<String, Object> data = new HashMap<>();
        List<CandidateHandle> handles = handleRepository.findByCandidateId(profile.getCandidate().getId());
        for (CandidateHandle h : handles) {
            try {
                Map<String, Object> result = switch (h.getSource()) {
                    case GITHUB -> gitHubCollector.collect(h.getHandle());
                    case STACKOVERFLOW -> stackOverflowCollector.collect(h.getHandle());
                    case TWITTER -> twitterCollector.collect(h.getHandle());
                    case LINKEDIN -> linkedInCollector.collect(h.getProfileUrl());
                    default -> Map.of("note","collector_not_implemented");
                };
                data.put(h.getSource().name(), result);
                saveRawData(profile.getCandidate(), h.getSource().name(), result);
            } catch (Exception e) {
                log.warn("Collector error for {} {}: {}", h.getSource(), h.getHandle(), e.getMessage());
            }
        }
        if (profile.getCandidate().getCvParsedJson() != null) {
            data.put("CV_PARSED", parseCvJson(profile.getCandidate().getCvParsedJson()));
        }
        return data;
    }

    private void saveRawData(Candidate candidate, String source, Map<String, Object> result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            Optional<RawSourceData> existing = rawSourceDataRepository.findByCandidateIdAndSource(candidate.getId(), source);
            RawSourceData raw = existing.orElse(RawSourceData.builder().candidate(candidate).source(source).build());
            raw.setJsonPayload(json); raw.setStale(false); raw.setFetchedAt(LocalDateTime.now());
            rawSourceDataRepository.save(raw);
        } catch (Exception e) { log.warn("Save raw data error: {}", e.getMessage()); }
    }

    private Map<String, Object> parseCvJson(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return Map.of("raw", json); }
    }

    @Transactional(readOnly=true)
    public ProfileSummaryResponse getSummary(UUID profileId) {
        Profile360 profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        List<ProfileScore> scores = scoreRepository.findByProfileId(profileId);
        List<RedFlag> flags = redFlagRepository.findByProfileId(profileId);
        ProfileScore composite = scores.stream().filter(s -> "COMPOSITE".equals(s.getDimension()))
            .findFirst().orElse(null);
        return ProfileSummaryResponse.builder()
            .profileId(profileId)
            .candidateName(profile.getCandidate().getName())
            .jobRoleName(profile.getJobRole().getName())
            .status(profile.getStatus().name())
            .compositeScore(composite != null ? composite.getCompositeScore() : null)
            .compositeLabel(composite != null ? composite.getCompositeLabel() : null)
            .dataCoveragePct(composite != null ? composite.getDataCoveragePct() : null)
            .dimensionScores(scores.stream().map(s -> ProfileSummaryResponse.DimensionScore.builder()
                .dimension(s.getDimension()).score(s.getScore()).label(s.getLabel()).weightUsed(s.getWeightUsed()).build())
                .collect(Collectors.toList()))
            .redFlags(flags.stream().map(f -> ProfileSummaryResponse.RedFlagItem.builder()
                .flagType(f.getFlagType()).description(f.getDescription())
                .severity(f.getSeverity().name()).source(f.getSource()).build())
                .collect(Collectors.toList()))
            .createdAt(profile.getCreatedAt()).refreshedAt(profile.getRefreshedAt())
            .build();
    }

    @Transactional
    public RecruiterNote addNote(UUID profileId, String noteText, User recruiter) {
        Profile360 profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        return noteRepository.save(RecruiterNote.builder()
            .profile(profile).recruiter(recruiter).noteText(noteText).build());
    }
}
