package com.predictflow.service;

import com.predictflow.entity.EventAudit;
import com.predictflow.repository.EventAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAuditServiceImpl implements EventAuditService {

    private final EventAuditRepository repo;

    public EventAudit record(String eventType, String payload, String source, String traceId) {
        EventAudit e = EventAudit.builder()
                .eventType(eventType)
                .payload(payload)
                .source(source)
                .traceId(traceId)
                .createdAt(Instant.now())
                .build();
        return repo.save(e);
    }

    public List<EventAudit> listRecent(int limit) {
        return repo.findAll().stream().limit(limit).toList();
    }
}