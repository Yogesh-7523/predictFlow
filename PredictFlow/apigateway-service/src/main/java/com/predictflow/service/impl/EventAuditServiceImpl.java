package com.predictflow.service.impl;

import com.predictflow.entity.EventAudit;
import com.predictflow.repository.EventAuditRepository;
import com.predictflow.service.EventAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAuditServiceImpl implements EventAuditService {

    private final EventAuditRepository repo;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public EventAudit record(String eventType, String payload, String source, String traceId) {
        String sql = "INSERT INTO predictflow.event_audit (event_type, payload, source, trace_id, created_at) " +
                "VALUES (?, ?::jsonb, ?, ?, ?) RETURNING id, created_at";

        Instant now = Instant.now();
        Object[] params = new Object[]{eventType, payload == null ? "{}" : payload, source, traceId, Timestamp.from(now)};

        EventAudit inserted = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
            EventAudit e = EventAudit.builder()
                    .id((int) rs.getLong("id"))
                    .eventType(eventType)
                    .payload(payload)
                    .source(source)
                    .traceId(traceId)
                    .createdAt(rs.getTimestamp("created_at").toInstant())
                    .build();
            return e;
        });

        if (inserted == null) {
            EventAudit e = EventAudit.builder()
                    .eventType(eventType)
                    .payload(payload)
                    .source(source)
                    .traceId(traceId)
                    .createdAt(now)
                    .build();
            return repo.save(e);
        }
        return inserted;
    }

    @Override
    public List<EventAudit> listRecent(int limit) {
        return repo.findAll().stream().limit(limit).toList();
    }
}