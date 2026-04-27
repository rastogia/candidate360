package com.recruit.c360.service.parser;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CvParseResult {
    private String rawText;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String twitterHandle;
    private String stackoverflowUrl;
    private List<String> skills;
    private List<String> employers;
    private List<String> jobTitles;
    private List<String> educationInstitutions;
    private Integer yearsOfExperience;
    private List<String> certifications;
    private List<String> languages;
}
