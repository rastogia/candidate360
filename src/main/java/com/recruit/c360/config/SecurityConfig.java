package com.recruit.c360.config;
import com.recruit.c360.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration @EnableWebSecurity @EnableMethodSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
            .map(u -> org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail()).password(u.getPassword())
                .roles(u.getRole().name()).build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
    @Bean public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService());
        p.setPasswordEncoder(passwordEncoder()); return p;
    }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }
    @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**","/swagger-ui/**","/api-docs/**","/swagger-ui.html").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
