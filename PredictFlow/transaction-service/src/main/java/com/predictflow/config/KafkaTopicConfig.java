package com.predictflow.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic txnFailedTopic() {
        return new NewTopic("txn.failed", 1, (short)1);
    }

    @Bean
    public NewTopic txnRetryTopic() {
        return new NewTopic("txn.retry", 1, (short)1);
    }

    @Bean
    public NewTopic txnEventTopic() {
        return new NewTopic("transaction-events", 1, (short)1);
    }
}