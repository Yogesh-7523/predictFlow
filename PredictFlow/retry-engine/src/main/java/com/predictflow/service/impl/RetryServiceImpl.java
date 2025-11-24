package com.predictflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.predictflow.dto.MlPredictionResponseDto;
import com.predictflow.dto.RetryEventDto;
import com.predictflow.dto.TxnFailedEventDto;
import com.predictflow.entity.RetryLog;
import com.predictflow.repository.RetryLogRepository;
import com.predictflow.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class RetryServiceImpl implements RetryService {

    private final RetryLogRepository retryLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${ml.service.url:http://localhost:5000/predict}")
    private String ML_URL;
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryServiceImpl.class);

    @Override
    @SneakyThrows
    public void handleTxnFailed(String payload) {
        TxnFailedEventDto evt = objectMapper.readValue(payload, TxnFailedEventDto.class);
        // call ML
        MlPredictionResponseDto pred = callMl(evt);
        int recommended = pred.getRecommendedDelay() != null ? pred.getRecommendedDelay() : 30;
        int applied = recommended;
        int attempt = evt.getRetryCount() != null ? evt.getRetryCount() + 1 : 1;

        // record log with status "SCHEDULED" and store both recommended and applied delays + attempt + notes
        RetryLog logEntry = RetryLog.builder()
                .txnId(evt.getTxnId())
                .predictedSuccess(pred.getProbSuccess())
                .recommendedDelaySec(recommended)
                .delayAppliedSec(applied)
                .attemptNumber(attempt)
                .retryTime(Instant.now().plusSeconds(applied))
                .retryStatus("SCHEDULED")
                .notes(objectMapper.writeValueAsString(evt))
                .build();
        retryLogRepository.save(logEntry);

        // schedule the retry publish
        scheduler.schedule(() -> publishRetry(evt.getTxnId(), pred, logEntry.getId(), applied, attempt), applied, TimeUnit.SECONDS);
    }

    @Override
    public RetryEventDto manualRetry(Long txnId) {
        // make a minimal request to ML with txnId only (real impl would fetch txn details)
        MlPredictionResponseDto pred = MlPredictionResponseDto.builder().probSuccess(0.5).recommendedDelay(5).build();
        int recommended = pred.getRecommendedDelay() != null ? pred.getRecommendedDelay() : 5;
        int applied = recommended;
        int attempt = 1;

        RetryLog logEntry = RetryLog.builder()
                .txnId(txnId)
                .predictedSuccess(pred.getProbSuccess())
                .recommendedDelaySec(recommended)
                .delayAppliedSec(applied)
                .attemptNumber(attempt)
                .retryTime(Instant.now().plusSeconds(applied))
                .retryStatus("MANUAL_SCHEDULED")
                .notes(null)
                .build();
        retryLogRepository.save(logEntry);

        scheduler.schedule(() -> publishRetry(txnId, pred, logEntry.getId(), applied, attempt), applied, TimeUnit.SECONDS);

        return RetryEventDto.builder()
                .txnId(txnId)
                .attempt(attempt)
                .prediction(pred.getProbSuccess())
                .build();
    }

    @Override
    public List<RetryLog> listLogs() {
        return retryLogRepository.findAll();
    }

    private MlPredictionResponseDto callMl(TxnFailedEventDto evt) {
        try {
            log.debug("Calling ML service at {} with payload: {}", ML_URL, evt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TxnFailedEventDto> req = new HttpEntity<>(evt, headers);
            ResponseEntity<MlPredictionResponseDto> resp = restTemplate.exchange(ML_URL, HttpMethod.POST, req, MlPredictionResponseDto.class);
            MlPredictionResponseDto body = resp.getBody();
            log.debug("ML response: status={} body={}", resp.getStatusCode(), body);
            return body == null ? MlPredictionResponseDto.builder().probSuccess(0.5).recommendedDelay(30).build() : body;
        } catch (Exception e) {
            log.error("ML call failed, using fallback prediction", e);
            return MlPredictionResponseDto.builder().probSuccess(0.5).recommendedDelay(30).build();
        }
    }

    // changed signature to accept applied delay and attempt so we can update log properly
    private void publishRetry(Long txnId, MlPredictionResponseDto pred, Long logId, int appliedDelay, int attempt) {
        try {
            RetryEventDto retry = RetryEventDto.builder()
                    .txnId(txnId)
                    .attempt(attempt)
                    .prediction(pred.getProbSuccess())
                    .build();
            log.debug("Publishing retry event to topic txn.retry: {}", retry);
            kafkaTemplate.send("txn.retry", String.valueOf(txnId), retry);
            // update log
            RetryLog logEntry = retryLogRepository.findById(logId).orElse(null);
            if (logEntry != null) {
                logEntry.setRetryStatus("PUBLISHED");
                logEntry.setDelayAppliedSec(appliedDelay);
                logEntry.setAttemptNumber(attempt);
                retryLogRepository.save(logEntry);
            }
        } catch (Exception ignored) {
        }
    }

}

















//package com.predictflow.service.impl;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.predictflow.dto.MlPredictionResponseDto;
//import com.predictflow.dto.RetryEventDto;
//import com.predictflow.dto.TxnFailedEventDto;
//import com.predictflow.entity.RetryLog;
//import com.predictflow.repository.RetryLogRepository;
//import com.predictflow.service.RetryService;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import org.springframework.beans.factory.annotation.Value;
//
//import org.springframework.http.*;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//

















//import java.time.Instant;
//import java.util.List;
//import java.util.concurrent.*;
//
//@Service
//@RequiredArgsConstructor
//public class RetryServiceImpl implements RetryService {
//
//    private final RetryLogRepository retryLogRepository;
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//    private final ObjectMapper objectMapper;
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//
////    private final String ML_URL = System.getProperty("ml.service.url", "http://localhost:5000/predict");
//
//    @Value("${ml.service.url:http://localhost:5000/predict}")
//    private String ML_URL;
//    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryServiceImpl.class);
//
//    @Override
//    @SneakyThrows
//    public void handleTxnFailed(String payload) {
//        TxnFailedEventDto evt = objectMapper.readValue(payload, TxnFailedEventDto.class);
//        // call ML
//        MlPredictionResponseDto pred = callMl(evt);
//        int delay = pred.getRecommendedDelay() != null ? pred.getRecommendedDelay() : 30;
//        // record log with status "SCHEDULED"
//        RetryLog log = RetryLog.builder()
//                .txnId(evt.getTxnId())
//                .predictedSuccess(pred.getProbSuccess())
//                .delaySeconds(delay)
//                .retryTime(Instant.now().plusSeconds(delay))
//                .statusAfterRetry("SCHEDULED")
//                .build();
//        retryLogRepository.save(log);
//
//        // schedule the retry publish
//        scheduler.schedule(() -> publishRetry(evt.getTxnId(), pred, log.getId()), delay, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public RetryEventDto manualRetry(Long txnId) {
//        // make a minimal request to ML with txnId only (real impl would fetch txn details)
//        MlPredictionResponseDto pred = MlPredictionResponseDto.builder().probSuccess(0.5).recommendedDelay(5).build();
//        RetryLog log = RetryLog.builder()
//                .txnId(txnId)
//                .predictedSuccess(pred.getProbSuccess())
//                .delaySeconds(pred.getRecommendedDelay())
//                .retryTime(Instant.now().plusSeconds(pred.getRecommendedDelay()))
//                .statusAfterRetry("MANUAL_SCHEDULED")
//                .build();
//        retryLogRepository.save(log);
//
//        scheduler.schedule(() -> publishRetry(txnId, pred, log.getId()), pred.getRecommendedDelay(), TimeUnit.SECONDS);
//
//        return RetryEventDto.builder()
//                .txnId(txnId)
//                .attempt(1)
//                .prediction(pred.getProbSuccess())
//                .build();
//    }
//
//    @Override
//    public List<RetryLog> listLogs() {
//        return retryLogRepository.findAll();
//    }
//
//
//
//
//    private MlPredictionResponseDto callMl(TxnFailedEventDto evt) {
//        try {
//            log.debug("Calling ML service at {} with payload: {}", ML_URL, evt);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<TxnFailedEventDto> req = new HttpEntity<>(evt, headers);
//            ResponseEntity<MlPredictionResponseDto> resp = restTemplate.exchange(ML_URL, HttpMethod.POST, req, MlPredictionResponseDto.class);
//            MlPredictionResponseDto body = resp.getBody();
//            log.debug("ML response: status={} body={}", resp.getStatusCode(), body);
//            return body == null ? MlPredictionResponseDto.builder().probSuccess(0.5).recommendedDelay(30).build() : body;
//        } catch (Exception e) {
//            log.error("ML call failed, using fallback prediction", e);
//            return MlPredictionResponseDto.builder().probSuccess(0.5).recommendedDelay(30).build();
//        }
//    }
//
//    private void publishRetry(Long txnId, MlPredictionResponseDto pred, Long logId) {
//        try {
//            RetryEventDto retry = RetryEventDto.builder()
//                    .txnId(txnId)
//                    .attempt(1)
//                    .prediction(pred.getProbSuccess())
//                    .build();
//            log.debug("Publishing retry event to topic txn.retry: {}", retry);
//            kafkaTemplate.send("txn.retry", String.valueOf(txnId), retry);
//            // update log
//            RetryLog logEntry = retryLogRepository.findById(logId).orElse(null);
//            if (logEntry != null) {
//                logEntry.setStatusAfterRetry("PUBLISHED");
//                retryLogRepository.save(logEntry);
//            }
//        } catch (Exception ignored) {
//        }
//    }
//
//}