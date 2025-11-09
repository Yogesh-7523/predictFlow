package com.predictflow.service.impl;

import com.predictflow.entity.Transaction;
import com.predictflow.event.TransactionEvent;
import com.predictflow.repository.TransactionRepository;
import com.predictflow.service.TransactionService;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private static final String TOPIC = "transaction-events";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Create a transaction and publish to Kafka + cache in Redis
     */

    public void testRedis() {
        try {
            LettuceConnectionFactory cf = null;
            if (redisTemplate.getConnectionFactory() instanceof LettuceConnectionFactory) {
                cf = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
            }
            if (cf != null) {
                logger.info("Redis CF -> host={}, port={}, db={}", cf.getHostName(), cf.getPort(), cf.getDatabase());
            } else {
                logger.info("Redis ConnectionFactory class: {}", redisTemplate.getConnectionFactory().getClass().getName());
            }

            redisTemplate.opsForValue().set("test-key", "test-value");
            Object value = redisTemplate.opsForValue().get("test-key");
            logger.info("App read-after-write test-key -> {}", value);
        } catch (Exception e) {
            logger.error("testRedis error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @CachePut(value = "txn", key = "#result.id")
    public Transaction createTransaction(Transaction txn) {
        txn.setStatus("PENDING");
        Transaction saved = transactionRepository.save(txn);
        if(txn.getCreatedAt()==null){
            saved.setCreatedAt(java.time.LocalDateTime.now());
        }

        // Publish to Kafka
        TransactionEvent event = new TransactionEvent(
                saved.getId(),
                saved.getUserEmail(),
                saved.getAmount(),
                saved.getMerchant(),
                saved.getStatus()
        );
        try {
            kafkaTemplate.send(TOPIC, event);
        } catch (Exception ex) {
            logger.warn("Kafka publish error: " + ex.getMessage());
            throw new RuntimeException("Kafka publish failed: " + ex.getMessage());
        }

        return saved;
    }

    /**
     * Get transaction by ID from Redis (if cached), else from DB
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
}
