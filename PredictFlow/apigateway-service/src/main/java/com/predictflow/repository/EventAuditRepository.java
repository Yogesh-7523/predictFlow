package com.predictflow.repository;

import com.predictflow.entity.EventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventAuditRepository extends JpaRepository<EventAudit, Integer> {
}