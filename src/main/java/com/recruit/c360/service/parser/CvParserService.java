package com.recruit.c360.service.parser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.*;
@Slf4j @Service @RequiredArgsConstructor
public class CvParserService {
    private final ObjectMapper objectMapper;
    private static final Pattern EMAIL_P  = Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_P  = Pattern.compile("(\\+?[\\d\\s\\-().]{7,20})");
    private static final Pattern GITHUB_P = Pattern.compile("github\\.com/([\\w\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LI_P     = Pattern.compile("linkedin\\.com/in/([\\w\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SO_P     = Pattern.compile("stackoverflow\\.com/users/(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TW_P     = Pattern.compile("(?:twitter|x)\\.com/([\\w]+)", Pattern.CASE_INSENSITIVE);
    private static final List<String> SKILL_KEYWORDS = List.of(
        "Java","Spring","Spring Boot","Hibernate","JPA","Maven","Gradle","Docker","Kubernetes",
        "AWS","GCP","Azure","Microservices","REST","GraphQL","Kafka","Redis","PostgreSQL","MySQL",
        "MongoDB","Angular","React","Vue","Python","JavaScript","TypeScript","CI/CD","Jenkins",
        "GitHub Actions","SonarQube","Linux","Git","Agile","Scrum","Design Patterns","TDD","DDD");

    public CvParseResult parse(MultipartFile file) {
        try {
            String text = extractText(file);
            return buildResult(text);
        } catch (Exception e) {
            log.error("CV parse error: {}", e.getMessage());
            return CvParseResult.builder().rawText("").skills(List.of()).build();
        }
    }

    private String extractText(MultipartFile file) throws Exception {
        String name = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        if (name.endsWith(".pdf")) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(doc);
            }
        } else if (name.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(file.getBytes()))) {
                StringBuilder sb = new StringBuilder();
                doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
                return sb.toString();
            }
        } else {
            return new String(file.getBytes());
        }
    }

    private CvParseResult buildResult(String text) {
        Matcher em = EMAIL_P.matcher(text);
        String email = em.find() ? em.group() : null;
        Matcher gh = GITHUB_P.matcher(text);
        String githubUrl = gh.find() ? "https://github.com/" + gh.group(1) : null;
        Matcher li = LI_P.matcher(text);
        String linkedinUrl = li.find() ? "https://linkedin.com/in/" + li.group(1) : null;
        Matcher so = SO_P.matcher(text);
        String soUrl = so.find() ? "https://stackoverflow.com/users/" + so.group(1) : null;
        Matcher tw = TW_P.matcher(text);
        String twitter = tw.find() ? tw.group(1) : null;
        Matcher ph = PHONE_P.matcher(text);
        String phone = ph.find() ? ph.group().trim() : null;
        List<String> skills = SKILL_KEYWORDS.stream()
            .filter(s -> text.toLowerCase().contains(s.toLowerCase())).toList();
        return CvParseResult.builder()
            .rawText(text).email(email).phone(phone)
            .githubUrl(githubUrl).linkedinUrl(linkedinUrl)
            .stackoverflowUrl(soUrl).twitterHandle(twitter)
            .skills(skills).employers(List.of()).jobTitles(List.of())
            .educationInstitutions(List.of()).certifications(List.of()).languages(List.of())
            .build();
    }

    public String toJson(CvParseResult result) {
        try { return objectMapper.writeValueAsString(result); }
        catch (Exception e) { return "{}"; }
    }
}
