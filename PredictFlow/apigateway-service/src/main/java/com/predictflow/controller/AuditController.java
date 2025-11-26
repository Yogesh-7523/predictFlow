package com.predictflow.controller;

import com.predictflow.entity.AuthBlacklist;
import com.predictflow.entity.EventAudit;
import com.predictflow.repository.AuthBlacklistRepository;
import com.predictflow.service.EventAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AuditController {

    private final EventAuditService auditService;
    private final AuthBlacklistRepository blacklistRepo;

    @GetMapping("/audits")
    public List<EventAudit> recentAudits(@RequestParam(defaultValue = "50") int limit) {
        return auditService.listRecent(limit);
    }

    @GetMapping("/blacklist")
    public List<AuthBlacklist> blacklist() {
        return blacklistRepo.findAll();
    }

    @PostMapping("/blacklist")
    public AuthBlacklist addToBlacklist(@RequestParam String token, @RequestParam(required = false) Long expiresAtEpoch) {
        String hash = sha256(token);
        Instant expires = expiresAtEpoch == null ? null : Instant.ofEpochSecond(expiresAtEpoch);
        AuthBlacklist b = AuthBlacklist.builder()
                .tokenHash(hash)
                .blacklistedAt(Instant.now())
                .expiresAt(expires)
                .build();
        return blacklistRepo.save(b);
    }

    private String sha256(String in) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(in.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}