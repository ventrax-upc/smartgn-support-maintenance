package com.ventrax.support.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.*;

@Configuration
public class SecurityConfiguration {
    private static void reject(HttpServletResponse response, int status)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"status\":" + status + ",\"title\":\"Request denied\"}");
    }

    @Bean
    org.springframework.security.core.userdetails.UserDetailsService noLocalUsers() {
        return username -> {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                    "IAM required");
        };
    }

    @Bean
    org.springframework.web.cors.CorsConfigurationSource cors(
            @Value("${ALLOWED_ORIGIN:http://localhost:4200}") String origin) {
        var config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-ID"));
        config.setExposedHeaders(List.of("X-Total-Count", "X-Page", "X-Page-Size"));
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain security(
            HttpSecurity h,
            AccessPolicy access,
            @Value("${smartgn.iam-url}") String iam,
            @Value("${smartgn.internal-token}") String secret)
            throws Exception {
        if (secret.length() < 32)
            throw new IllegalArgumentException(
                    "Service credential must contain at least 32 characters");
        var factory =
                new org.springframework.http.client.JdkClientHttpRequestFactory(
                        java.net.http.HttpClient.newBuilder()
                                .connectTimeout(java.time.Duration.ofSeconds(2))
                                .build());
        factory.setReadTimeout(java.time.Duration.ofSeconds(3));
        var client =
                RestClient.builder()
                        .baseUrl(iam)
                        .requestFactory(factory)
                        .defaultHeader("Authorization", "Bearer " + secret)
                        .build();
        return h.cors(c -> {})
                .csrf(c -> c.disable())
                .sessionManagement(
                        c ->
                                c.sessionCreationPolicy(
                                        org.springframework.security.config.http
                                                .SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        a ->
                                a.requestMatchers(
                                                "/error",
                                                "/actuator/health/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .addFilterBefore(
                        new OncePerRequestFilter() {
                            protected void doFilterInternal(
                                    HttpServletRequest q, HttpServletResponse r, FilterChain next)
                                    throws java.io.IOException, ServletException {
                                String path = q.getRequestURI();
                                if (q.getMethod().equals("OPTIONS")) {
                                    next.doFilter(q, r);
                                    return;
                                }
                                if (path.startsWith("/actuator/health")
                                        || path.startsWith("/swagger")
                                        || path.startsWith("/v3/api-docs")) {
                                    next.doFilter(q, r);
                                    return;
                                }
                                try {
                                    String auth = q.getHeader("Authorization");
                                    if (auth == null || !auth.startsWith("Bearer ")) {
                                        reject(r, 401);
                                        return;
                                    }
                                    if (path.startsWith("/internal/")) {
                                        if (!java.security.MessageDigest.isEqual(
                                                auth.substring(7)
                                                        .getBytes(
                                                                java.nio.charset.StandardCharsets
                                                                        .UTF_8),
                                                secret.getBytes(
                                                        java.nio.charset.StandardCharsets.UTF_8))) {
                                            reject(r, 401);
                                            return;
                                        }
                                        SecurityContextHolder.getContext()
                                                .setAuthentication(
                                                        new UsernamePasswordAuthenticationToken(
                                                                "service", null, List.of()));
                                    } else {
                                        Map actor =
                                                client.post()
                                                        .uri("/internal/v1/sessions/introspect")
                                                        .body(Map.of("token", auth.substring(7)))
                                                        .retrieve()
                                                        .body(Map.class);
                                        if (actor == null
                                                || !"ACTIVE".equals(actor.get("status"))
                                                || !Set.of("USER", "SUPERADMIN")
                                                        .contains(actor.get("role"))) {
                                            reject(r, 401);
                                            return;
                                        }
                                        q.setAttribute("actor", actor);
                                        SecurityContextHolder.getContext()
                                                .setAuthentication(
                                                        new UsernamePasswordAuthenticationToken(
                                                                actor,
                                                                null,
                                                                List.of(
                                                                        new SimpleGrantedAuthority(
                                                                                "ROLE_"
                                                                                        + actor.get(
                                                                                                "role")))));
                                        access.checkRoute(q, actor);
                                    }
                                } catch (
                                        org.springframework.web.client.HttpClientErrorException e) {
                                    reject(r, 401);
                                    return;
                                } catch (org.springframework.web.server.ResponseStatusException e) {
                                    reject(r, e.getStatusCode().value());
                                    return;
                                } catch (org.springframework.web.client.RestClientException e) {
                                    reject(r, 503);
                                    return;
                                } catch (IllegalArgumentException e) {
                                    reject(r, 400);
                                    return;
                                }
                                next.doFilter(q, r);
                            }
                        },
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
