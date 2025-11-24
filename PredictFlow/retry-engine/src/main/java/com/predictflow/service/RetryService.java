package com.predictflow.service;

import com.predictflow.dto.RetryEventDto;
import com.predictflow.entity.RetryLog;

import java.util.List;

public interface RetryService {
    void handleTxnFailed(String payload);
    RetryEventDto manualRetry(Long txnId);
    List<RetryLog> listLogs();

}