package com.hdtpt.pentachat.finance.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.hdtpt.pentachat.finance.repository.WalletRepository;
import com.hdtpt.pentachat.finance.repository.TransactionRepository;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.finance.model.Transaction;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.finance.model.Wallet;

import java.util.List;

/**
 * Wallet service
 * Handles wallet operations: deposit, withdraw, transfer, balance inquiry
 * 
 * Business logic layer - does NOT depend directly on mock data
 * Depends on DataApi interface for data access
 * Enforces business rules (e.g., sufficient balance checks)
 */
@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get wallet balance for a user
     * If wallet doesn't exist, create one with 0 balance
     * 
     * @param userId user's ID
     * @return wallet with balance
     * @throws AppException if user not found
     */
    public Wallet getBalance(Long userId) {
        return walletRepository.findById(userId)
                .orElseGet(() -> {
                    // Create wallet if not found
                    Wallet wallet = Wallet.builder()
                            .userId(userId)
                            .balance(0.0)
                            .build();
                    return walletRepository.save(wallet);
                });
    }

    /**
     * Deposit money into user's wallet
     * 
     * @param userId user's ID
     * @param amount amount to deposit
     * @return updated wallet
     * @throws AppException if user/wallet not found or amount is invalid
     */
    @Transactional
    public Wallet deposit(Long userId, Double amount) {
        // Validate input
        if (amount == null || amount <= 0) {
            throw new AppException("Deposit amount must be greater than 0");
        }

        // Get current wallet
        Wallet wallet = getBalance(userId);

        // Update balance
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = Transaction.builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .fromUserId(userId)
                .amount(amount)
                .build();
        transactionRepository.save(transaction);

        return wallet;
    }

    @Transactional
    public Wallet awardGems(Long userId, Integer gems) {
        if (gems == null || gems <= 0) {
            return getBalance(userId);
        }

        return deposit(userId, gems.doubleValue());
    }

    /**
     * Withdraw money from user's wallet
     * 
     * @param userId user's ID
     * @param amount amount to withdraw
     * @return updated wallet
     * @throws AppException if user/wallet not found, amount invalid, or
     *                      insufficient balance
     */
    @Transactional
    public Wallet withdraw(Long userId, Double amount) {
        // Validate input
        if (amount == null || amount <= 0) {
            throw new AppException("Withdrawal amount must be greater than 0");
        }

        // Get current wallet
        Wallet wallet = getBalance(userId);

        // Check sufficient balance
        if (wallet.getBalance() < amount) {
            throw new AppException("Insufficient balance. Current balance: " + wallet.getBalance());
        }

        // Update balance
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = Transaction.builder()
                .type(Transaction.TransactionType.WITHDRAW)
                .fromUserId(userId)
                .amount(amount)
                .build();
        transactionRepository.save(transaction);

        return wallet;
    }

    /**
     * Transfer money from one user to another
     * 
     * @param fromUserId sender's ID
     * @param toUsername recipient's username
     * @param amount     amount to transfer
     * @return updated wallet of sender
     * @throws AppException if users not found, amount invalid, or insufficient
     *                      balance
     */

    @Transactional
    public Wallet transfer(Long fromUserId, String toUsername, Double amount) {
        // Validate input
        if (amount == null || amount <= 0) {
            throw new AppException("Transfer amount must be greater than 0");
        }

        // Get sender's wallet and check balance
        Wallet fromWallet = getBalance(fromUserId);
        if (fromWallet.getBalance() < amount) {
            throw new AppException("Insufficient balance. Current balance: " + fromWallet.getBalance());
        }

        // Get recipient
        User toUser = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new AppException("Recipient not found"));

        // Check recipient is not the same as sender
        if (fromUserId.equals(toUser.getId())) {
            throw new AppException("Cannot transfer to yourself");
        }

        // Get recipient's wallet
        Wallet toWallet = walletRepository.findById(toUser.getId())
                .orElseThrow(() -> new AppException("Recipient wallet not found"));

        // Update balances
        fromWallet.setBalance(fromWallet.getBalance() - amount);
        toWallet.setBalance(toWallet.getBalance() + amount);

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Record transaction
        Transaction transaction = Transaction.builder()
                .type(Transaction.TransactionType.TRANSFER)
                .fromUserId(fromUserId)
                .toUserId(toUser.getId())
                .amount(amount)
                .build();
        transactionRepository.save(transaction);

        return fromWallet;
    }

    /**
     * Get transaction history for a user
     * 
     * @param userId user's ID
     * @return list of transactions
     * @throws AppException if user not found
     */
    public List<Transaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByFromUserIdOrToUserId(userId, userId);
    }

    public void createWallet(Long userId, Double initialBalance) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance)
                .build();
        walletRepository.save(wallet);
    }
}
