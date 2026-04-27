package com.recruit.c360.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.c360.dto.request.*;
import com.recruit.c360.dto.response.CandidateResponse;
import com.recruit.c360.entity.*;
import com.recruit.c360.exception.*;
import com.recruit.c360.repository.*;
import com.recruit.c360.service.collector.HandleDiscoveryService;
import com.recruit.c360.service.parser.*;
import com.recruit.c360.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor @Transactional
public class CandidateService {
    private final CandidateRepository candidateRepository;
    private final CandidateHandleRepository handleRepository;
    private final CvParserService cvParserService;
    private final HandleDiscoveryService discoveryService;
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;

    public CandidateResponse createCandidate(CreateCandidateRequest req) {
        Candidate candidate = Candidate.builder()
            .name(req.getName()).email(req.getEmail())
            .phone(req.getPhone()).location(req.getLocation()).build();
        final Candidate saved = candidateRepository.save(candidate);
        List<CandidateHandle> discovered = discoveryService.discoverHandles(saved.getName(), saved.getEmail());
        discovered.forEach(h -> h.setCandidate(saved));
        handleRepository.saveAll(discovered);
        return toResponse(saved, discovered);
    }

    public CandidateResponse uploadCv(UUID candidateId, MultipartFile file) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));
        try {
            Path dir = Paths.get(storageProperties.getCvUploadDir());
            Files.createDirectories(dir);
            String fileName = candidateId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path dest = dir.resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            CvParseResult parsed = cvParserService.parse(file);
            candidate.setCvFilePath(dest.toString());
            candidate.setCvFileName(file.getOriginalFilename());
            candidate.setCvParsedJson(cvParserService.toJson(parsed));
            if (candidate.getEmail() == null && parsed.getEmail() != null) candidate.setEmail(parsed.getEmail());
            if (candidate.getPhone() == null && parsed.getPhone() != null) candidate.setPhone(parsed.getPhone());
            if (candidate.getLocation() == null && parsed.getLocation() != null) candidate.setLocation(parsed.getLocation());
            final Candidate updated = candidateRepository.save(candidate);
            enrichHandlesFromCv(updated, parsed);
            List<CandidateHandle> handles = handleRepository.findByCandidateId(candidateId);
            return toResponse(updated, handles);
        } catch (IOException e) {
            throw new BadRequestException("CV upload failed: " + e.getMessage());
        }
    }

    private void enrichHandlesFromCv(Candidate candidate, CvParseResult parsed) {
        if (parsed.getGithubUrl() != null) addHandle(candidate, CandidateHandle.DataSource.GITHUB,
            parsed.getGithubUrl().replace("https://github.com/",""), parsed.getGithubUrl());
        if (parsed.getLinkedinUrl() != null) addHandle(candidate, CandidateHandle.DataSource.LINKEDIN,
            parsed.getLinkedinUrl().replace("https://linkedin.com/in/",""), parsed.getLinkedinUrl());
        if (parsed.getStackoverflowUrl() != null) addHandle(candidate, CandidateHandle.DataSource.STACKOVERFLOW,
            parsed.getStackoverflowUrl(), parsed.getStackoverflowUrl());
        if (parsed.getTwitterHandle() != null) addHandle(candidate, CandidateHandle.DataSource.TWITTER,
            parsed.getTwitterHandle(), "https://twitter.com/"+parsed.getTwitterHandle());
    }

    private void addHandle(Candidate candidate, CandidateHandle.DataSource source, String handle, String url) {
        List<CandidateHandle> existing = handleRepository.findByCandidateId(candidate.getId());
        boolean alreadyExists = existing.stream().anyMatch(h -> h.getSource() == source);
        if (!alreadyExists) {
            handleRepository.save(CandidateHandle.builder()
                .candidate(candidate).source(source).handle(handle)
                .profileUrl(url).confirmed(true)
                .discoveryMethod(CandidateHandle.DiscoveryMethod.AUTO_DISCOVERED).build());
        }
    }

    public CandidateResponse addHandle(UUID candidateId, AddHandleRequest req) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));
        CandidateHandle handle = CandidateHandle.builder()
            .candidate(candidate).source(req.getSource()).handle(req.getHandle())
            .profileUrl(req.getProfileUrl()).confirmed(true)
            .discoveryMethod(CandidateHandle.DiscoveryMethod.MANUAL).build();
        handleRepository.save(handle);
        return toResponse(candidate, handleRepository.findByCandidateId(candidateId));
    }

    @Transactional(readOnly=true)
    public CandidateResponse getCandidate(UUID candidateId) {
        Candidate c = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));
        return toResponse(c, handleRepository.findByCandidateId(candidateId));
    }

    @Transactional(readOnly=true)
    public List<CandidateResponse> searchCandidates(String name) {
        return candidateRepository.searchByName(name).stream()
            .map(c -> toResponse(c, handleRepository.findByCandidateId(c.getId()))).toList();
    }

    private CandidateResponse toResponse(Candidate c, List<CandidateHandle> handles) {
        return CandidateResponse.builder()
            .id(c.getId()).name(c.getName()).email(c.getEmail())
            .phone(c.getPhone()).location(c.getLocation()).cvFileName(c.getCvFileName())
            .createdAt(c.getCreatedAt())
            .handles(handles.stream().map(h -> CandidateResponse.HandleResponse.builder()
                .id(h.getId()).source(h.getSource().name()).handle(h.getHandle())
                .profileUrl(h.getProfileUrl()).discoveryMethod(h.getDiscoveryMethod().name())
                .confirmed(h.isConfirmed()).build()).collect(Collectors.toList()))
            .build();
    }
}
