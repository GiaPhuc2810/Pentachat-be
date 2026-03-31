package com.hdtpt.pentachat.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.finance.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
}
