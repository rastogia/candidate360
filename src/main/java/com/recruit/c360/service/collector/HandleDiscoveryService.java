package com.recruit.c360.service.collector;
import com.recruit.c360.entity.CandidateHandle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;
@Slf4j @Service @RequiredArgsConstructor
public class HandleDiscoveryService {
    private final WebSearchCollector searchCollector;
    private static final Pattern GH_P  = Pattern.compile("github\\.com/([\\w\\-]+)(?!/[\\w\\-]+/)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LI_P  = Pattern.compile("linkedin\\.com/in/([\\w\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SO_P  = Pattern.compile("stackoverflow\\.com/users/(\\d+)", Pattern.CASE_INSENSITIVE);

    public List<CandidateHandle> discoverHandles(String name, String email) {
        List<CandidateHandle> handles = new ArrayList<>();
        try {
            String query = name + " developer github linkedin stackoverflow";
            Map<String,Object> results = searchCollector.search(query);
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> organic = (List<Map<String,Object>>) results.getOrDefault("organic", List.of());
            Set<String> seen = new HashSet<>();
            for (Map<String,Object> r : organic) {
                String link = String.valueOf(r.getOrDefault("link",""));
                Matcher gh = GH_P.matcher(link);
                if (gh.find() && seen.add("github:"+gh.group(1))) {
                    handles.add(CandidateHandle.builder()
                        .source(CandidateHandle.DataSource.GITHUB)
                        .handle(gh.group(1))
                        .profileUrl("https://github.com/"+gh.group(1))
                        .discoveryMethod(CandidateHandle.DiscoveryMethod.AUTO_DISCOVERED)
                        .confirmed(false).build());
                }
                Matcher li = LI_P.matcher(link);
                if (li.find() && seen.add("linkedin:"+li.group(1))) {
                    handles.add(CandidateHandle.builder()
                        .source(CandidateHandle.DataSource.LINKEDIN)
                        .handle(li.group(1))
                        .profileUrl("https://linkedin.com/in/"+li.group(1))
                        .discoveryMethod(CandidateHandle.DiscoveryMethod.AUTO_DISCOVERED)
                        .confirmed(false).build());
                }
                Matcher so = SO_P.matcher(link);
                if (so.find() && seen.add("so:"+so.group(1))) {
                    handles.add(CandidateHandle.builder()
                        .source(CandidateHandle.DataSource.STACKOVERFLOW)
                        .handle(so.group(1))
                        .profileUrl("https://stackoverflow.com/users/"+so.group(1))
                        .discoveryMethod(CandidateHandle.DiscoveryMethod.AUTO_DISCOVERED)
                        .confirmed(false).build());
                }
            }
        } catch (Exception e) {
            log.error("Handle discovery error: {}", e.getMessage());
        }
        return handles;
    }
}
