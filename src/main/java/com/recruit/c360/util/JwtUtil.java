package com.recruit.c360.util;
import com.recruit.c360.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component @RequiredArgsConstructor
public class JwtUtil {
    private final AppProperties appProperties;
    private SecretKey key() {
        return Keys.hmacShaKeyFor(appProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(UserDetails ud) {
        return Jwts.builder().subject(ud.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + appProperties.getExpirationMs()))
            .signWith(key()).compact();
    }
    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
    public boolean isValid(String token, UserDetails ud) {
        try { return extractUsername(token).equals(ud.getUsername()) &&
                     !Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (Exception e) { return false; }
    }
}
