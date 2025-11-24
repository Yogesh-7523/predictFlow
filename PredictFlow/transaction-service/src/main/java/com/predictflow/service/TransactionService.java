package com.predictflow.service;


import com.predictflow.entity.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(Transaction txn);
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByUser(String userEmail);
    void testRedis();
}
