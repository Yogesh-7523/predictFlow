package com.predictflow.controller;

import com.predictflow.dto.RetryEventDto;
import com.predictflow.entity.RetryLog;
import com.predictflow.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/retry")
@RequiredArgsConstructor
public class RetryController {

    private final RetryService retryService;

    @GetMapping("/logs")
    public ResponseEntity<List<RetryLog>> getLogs() {
        return ResponseEntity.ok(retryService.listLogs());
    }

    @PostMapping("/manual/{txnId}")
    public ResponseEntity<RetryEventDto> manualRetry(@PathVariable Long txnId) {
        RetryEventDto event = retryService.manualRetry(txnId);
        return ResponseEntity.ok(event);
    }
}