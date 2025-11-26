package com.predictflow.repository;

import com.predictflow.entity.AuthBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthBlacklistRepository extends JpaRepository<AuthBlacklist, Integer> {
    boolean existsByTokenHash(String tokenHash);
    Optional<AuthBlacklist> findByTokenHash(String tokenHash);
}