package com.recruit.c360.service.scoring;
import com.recruit.c360.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
@Slf4j @Service
public class RedFlagEngine {
    @SuppressWarnings("unchecked")
    public List<RedFlag> detect(Profile360 profile, Map<String, Object> enrichedData) {
        List<RedFlag> flags = new ArrayList<>();
        Object aiFlags = enrichedData.get("potentialRedFlags");
        if (aiFlags instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?,?> m) {
                    String ft   = String.valueOf(m.getOrDefault("flagType","UNKNOWN"));
                    String desc = String.valueOf(m.getOrDefault("description",""));
                    String sev  = String.valueOf(m.getOrDefault("severity","LOW")).toUpperCase();
                    RedFlag.Severity severity;
                    try { severity = RedFlag.Severity.valueOf(sev); }
                    catch (Exception e) { severity = RedFlag.Severity.LOW; }
                    flags.add(RedFlag.builder()
                        .profile(profile).flagType(ft).description(desc)
                        .severity(severity).source("AI_ANALYSIS").build());
                }
            }
        }
        Object consistency = enrichedData.get("consistencyScore");
        if (consistency instanceof Integer i && i < 30) {
            flags.add(RedFlag.builder().profile(profile)
                .flagType("LOW_CONSISTENCY").description("Candidate shows low activity consistency score: " + i)
                .severity(RedFlag.Severity.MEDIUM).source("SCORING").build());
        }
        Object repStr = String.valueOf(enrichedData.getOrDefault("communityReputation",""));
        if (repStr.toLowerCase().contains("toxic") || repStr.toLowerCase().contains("negative")) {
            flags.add(RedFlag.builder().profile(profile)
                .flagType("NEGATIVE_COMMUNITY_BEHAVIOUR")
                .description("Community reputation analysis indicates potential negative behaviour")
                .severity(RedFlag.Severity.HIGH).source("AI_ANALYSIS").build());
        }
        return flags;
    }
}
