package com.predictflow.controller;

import com.predictflow.entity.Transaction;
import com.predictflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.hibernate.PropertyValueException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/txn")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/create")
    public ResponseEntity<?> createTxn(@RequestBody Transaction txn) {
        try {
            Transaction created = transactionService.createTransaction(txn);
            return ResponseEntity.ok(created);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request data. Please check all required fields and references."));
        } catch (PropertyValueException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Missing or invalid required field."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred. Please try again later."));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<Transaction> txns = transactionService.getAllTransactions();
            return ResponseEntity.ok(txns);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Unable to fetch transactions at this time."));
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<?> getByUser(@PathVariable String email) {
        try {
            List<Transaction> txns = transactionService.getTransactionsByUser(email);
            return ResponseEntity.ok(txns);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Unable to fetch transactions for the specified user."));
        }
    }

    // Simple error response DTO
    static class ErrorResponse {
        private final String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }


    @GetMapping("/test-redis")
    public ResponseEntity<?> testRedis() {
        try {
            transactionService.testRedis();
            return ResponseEntity.ok(new ErrorResponse("Redis test key set"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Redis test failed: " + e.getMessage()));
        }
    }
}