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
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private final AuthBlacklistRepository blacklistRepo;
    private final EventAuditService auditService;

    @Value("${jwt.secret:your-secret-key-change-in-production}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        try {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // check blacklist
                String tokenHash = sha256(token);
                if (blacklistRepo.existsByTokenHash(tokenHash)) {
                    log.warn("Blocked request with blacklisted token for path={}", path);
                    auditService.record("gateway.request.blocked", "{\"path\":\"" + path + "\"}", "gateway", null);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // validate token signature
                Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token);

                // optional: record successful auth audit
                auditService.record("gateway.request.authenticated", "{\"path\":\"" + path + "\"}", "gateway", null);
                log.debug("Token validated for path={}", path);

            } else if (!isPublicRoute(path)) {
                log.warn("Unauthorized request to protected path={}", path);
                auditService.record("gateway.request.unauthenticated", "{\"path\":\"" + path + "\"}", "gateway", null);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } else {
                // public route — record audit optionally
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

    @Override
    public int getOrder() {
        return -1;
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
}