package com.predictflow;

import com.predictflow.service.TransactionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
        System.out.println("Transaction Service is running on port 8082...");
    }


    @Bean
    public CommandLineRunner redisStartupCheck(TransactionService transactionService) {
        return args -> {
            try {
                transactionService.testRedis();
                System.out.println("Redis startup test OK");
            } catch (Exception ex) {
                System.err.println("Redis startup test failed: " + ex.getMessage());
            }
        };
    }
}
