package com.hdtpt.pentachat.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.finance.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
