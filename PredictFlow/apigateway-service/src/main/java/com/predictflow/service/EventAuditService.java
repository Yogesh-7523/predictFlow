package com.predictflow.service;

import com.predictflow.entity.EventAudit;

import java.util.List;

public interface EventAuditService {
    public EventAudit record(String eventType, String payload, String source, String traceId);
    public List<EventAudit> listRecent(int limit);
}
