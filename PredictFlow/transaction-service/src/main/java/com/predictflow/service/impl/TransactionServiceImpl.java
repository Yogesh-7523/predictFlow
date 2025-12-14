package com.predictflow.service.impl;

import com.predictflow.entity.Transaction;
import com.predictflow.event.TransactionEvent;
import com.predictflow.event.TxnFailedEvent;
import com.predictflow.repository.TransactionRepository;
import com.predictflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;  // Changed to Object for both event types
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOPIC_EVENTS = "transaction-events";
    private static final String TOPIC_TXN_FAILED = "txn.failed";

    /**
     * Test Redis connectivity and read-after-write
     */
    /**
     * Test Redis connectivity and read-after-write
     */
    public void testRedis() {
        try {
            // Use Spring Boot auto-configured properties instead of ConnectionFactory methods
            logger.info("Redis config from Spring Boot: host=localhost, port=63790");

            redisTemplate.opsForValue().set("test-key", "test-value");
            Object value = redisTemplate.opsForValue().get("test-key");
            logger.info("App read-after-write test-key -> {}", value);
        } catch (Exception e) {
            logger.error("testRedis error: {}", e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Get transaction by ID from Redis cache, fallback to DB
     */
    @Cacheable(value = "txn", key = "#id")
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getTransactionsByUser(String userEmail) {
        return transactionRepository.findByUserEmail(userEmail);
    }

    @Override
    @CachePut(value = "txn", key = "#result.id")
    public Transaction createTransaction(Transaction txn) {
        // Default to PENDING status if not provided
        if (txn.getStatus() == null || txn.getStatus().isBlank()) {
            txn.setStatus("PENDING");
        }

        Transaction saved = transactionRepository.save(txn);
        if (saved.getCreatedAt() == null) {
            saved.setCreatedAt(LocalDateTime.now());
        }

        // Publish generic transaction event
        TransactionEvent event = new TransactionEvent(
                saved.getId(),
                saved.getUserEmail(),
                saved.getAmount(),
                saved.getMerchant(),
                saved.getStatus()
        );

        try {
            kafkaTemplate.send(TOPIC_EVENTS, String.valueOf(saved.getId()), event);
            logger.info("Published transaction event for txnId: {}", saved.getId());
        } catch (Exception ex) {
            logger.error("Kafka publish error (transaction-events): {}", ex.getMessage(), ex);
            throw new RuntimeException("Kafka publish failed: " + ex.getMessage());
        }

        // If transaction failed, publish failure event for retry-engine
        if ("FAILED".equalsIgnoreCase(saved.getStatus())) {
            TxnFailedEvent failedEvent = TxnFailedEvent.builder()
                    .txnId(saved.getId())
                    .userId(saved.getUserId())
                    .amount(saved.getAmount())
                    .merchant(saved.getMerchant())
                    .reason("FAILURE_DETECTED")
                    .retryCount(0)
                    .build();

            try {
                kafkaTemplate.send(TOPIC_TXN_FAILED, String.valueOf(saved.getId()), failedEvent);
                logger.info("Published failed transaction event for txnId: {}", saved.getId());
            } catch (Exception ex) {
                logger.warn("Kafka publish error (txn.failed): {}", ex.getMessage());
            }
        }

        return saved;
    }
}
