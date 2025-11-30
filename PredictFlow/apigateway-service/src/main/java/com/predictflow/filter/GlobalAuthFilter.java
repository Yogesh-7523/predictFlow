package com.predictflow.filter;

import com.predictflow.repository.AuthBlacklistRepository;
import com.predictflow.service.EventAuditService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private final AuthBlacklistRepository blacklistRepo;
    private final EventAuditService auditService;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    private byte[] signingKeyBytes() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new RuntimeException("jwt.secret is not configured");
        }
        try {
            // Treat configured value as Base64 if it looks like Base64; otherwise use raw bytes
            if (jwtSecret.matches("^[A-Za-z0-9+/=]+$") && jwtSecret.length() % 4 == 0) {
                return Base64.getDecoder().decode(jwtSecret);
            } else {
                return jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            return jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        try {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                String tokenHash = sha256(token);
                if (blacklistRepo.existsByTokenHash(tokenHash)) {
                    auditService.record("gateway.request.blocked", "{\"path\":\"" + path + "\"}", "gateway", null);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                byte[] keyBytes = signingKeyBytes();
                Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                        .build()
                        .parseClaimsJws(token);

                auditService.record("gateway.request.authenticated", "{\"path\":\"" + path + "\"}", "gateway", null);
            } else if (!isPublicRoute(path)) {
                auditService.record("gateway.request.unauthenticated", "{\"path\":\"" + path + "\"}", "gateway", null);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } else {
                auditService.record("gateway.request.public", "{\"path\":\"" + path + "\"}", "gateway", null);
            }
        } catch (Exception e) {
            log.error("Auth check failed: {}", e.getMessage());
            auditService.record("gateway.request.auth_error", "{\"path\":\"" + path + "\",\"error\":\"" + e.getMessage() + "\"}", "gateway", null);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private boolean isPublicRoute(String path) {
        return path.equals("/") ||
                path.startsWith("/health") ||
                path.startsWith("/actuator") ||
                path.startsWith("/auth/") ||
                path.startsWith("/api/auth/");
    }

    private String sha256(String in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(in.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrder() { return -1; }
}