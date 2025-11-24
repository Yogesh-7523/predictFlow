package com.predictflow.consumer;

import com.predictflow.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TxnFailedConsumer {

    private final RetryService retryService;

    @KafkaListener(topics = "txn.failed", groupId = "retry-engine-group")
    public void onTxnFailed(String message) {
        // delegate to service for ML call and scheduling
        retryService.handleTxnFailed(message);
    }
}