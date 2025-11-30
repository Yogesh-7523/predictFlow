package com.predictflow.controller;

import com.predictflow.entity.AuthBlacklist;
import com.predictflow.repository.AuthBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AuthBlacklistRepository blacklistRepo;

    // changed path to avoid duplicate mapping with AuditController
    @PostMapping("/admin/blacklist/add")
    public ResponseEntity<?> blacklist(@RequestParam("token") String token) {
        try {
            String t = token == null ? "" : token.trim();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(t.getBytes(StandardCharsets.UTF_8));
            String hash = Base64.getEncoder().encodeToString(digest);

            if (blacklistRepo.existsByTokenHash(hash)) {
                return ResponseEntity.status(409).body("token already blacklisted");
            }

            AuthBlacklist entry = AuthBlacklist.builder()
                    .tokenHash(hash)
                    .blacklistedAt(Instant.now())
                    .build();
            blacklistRepo.save(entry);
            return ResponseEntity.ok(hash);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}