package com.hdtpt.pentachat.finance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hdtpt.pentachat.finance.model.Transaction;
import com.hdtpt.pentachat.finance.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Transaction-related business logic
 * Handles transaction creation and retrieval operations
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new transaction
     * 
     * @param type       Transaction type (DEPOSIT, WITHDRAW, TRANSFER)
     * @param fromUserId Sender user ID
     * @param toUserId   Recipient user ID (null for DEPOSIT/WITHDRAW)
     * @param amount     Transaction amount
     * @return Created transaction entity
     */
    @Transactional
    public Transaction createTransaction(
            Transaction.TransactionType type,
            Long fromUserId,
            Long toUserId,
            Double amount) {
        Transaction tx = Transaction.builder()
                .type(type)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .amount(amount)
                .build();

        return transactionRepository.save(tx);
    }

    /**
     * Get all transactions for a user (sent or received)
     * 
     * @param userId User ID
     * @return List of transactions
     */
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByFromUserIdOrToUserId(userId, userId);
    }
}
