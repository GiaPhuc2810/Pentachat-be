package com.hdtpt.pentachat.games.repository;

import com.hdtpt.pentachat.games.model.GamePointWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePointWalletRepository extends JpaRepository<GamePointWallet, Long> {
}
