package com.predictflow.repository;

import com.predictflow.entity.RetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetryLogRepository extends JpaRepository<RetryLog, Long> {
}