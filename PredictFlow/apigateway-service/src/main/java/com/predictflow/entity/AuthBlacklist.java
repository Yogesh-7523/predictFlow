package com.predictflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "auth_blacklist", schema = "predictflow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 512)
    private String tokenHash;

    @Column(name = "blacklisted_at", nullable = false)
    private Instant blacklistedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}