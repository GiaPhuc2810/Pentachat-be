package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.finance.service.WalletService;
import com.hdtpt.pentachat.games.dto.response.GamePointWalletResponse;
import com.hdtpt.pentachat.games.model.GamePointWallet;
import com.hdtpt.pentachat.games.repository.GamePointWalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GamePointWalletService {

    public static final int EXCHANGE_THRESHOLD = 1000;

    private final GamePointWalletRepository gamePointWalletRepository;
    private final WalletService walletService;

    public GamePointWalletService(GamePointWalletRepository gamePointWalletRepository, WalletService walletService) {
        this.gamePointWalletRepository = gamePointWalletRepository;
        this.walletService = walletService;
    }

    public GamePointWalletResponse getWallet(Long userId) {
        return toResponse(getOrCreateWallet(userId));
    }

    @Transactional
    public GamePointWalletResponse addPoints(Long userId, Integer points) {
        if (points == null || points <= 0) {
            return getWallet(userId);
        }

        GamePointWallet wallet = getOrCreateWallet(userId);
        wallet.setTotalPoints(wallet.getTotalPoints() + points);
        wallet.setAvailablePoints(wallet.getAvailablePoints() + points);
        return toResponse(gamePointWalletRepository.save(wallet));
    }

    @Transactional
    public GamePointWalletResponse exchangePoints(Long userId, Integer points) {
        if (points == null || points < EXCHANGE_THRESHOLD || points % EXCHANGE_THRESHOLD != 0) {
            throw new RuntimeException("Points exchange must be a multiple of 1000");
        }

        GamePointWallet wallet = getOrCreateWallet(userId);
        if (wallet.getAvailablePoints() < points) {
            throw new RuntimeException("Khong du diem de doi gem");
        }

        int gems = points / EXCHANGE_THRESHOLD;
        wallet.setAvailablePoints(wallet.getAvailablePoints() - points);
        wallet.setTotalGemsConverted(wallet.getTotalGemsConverted() + gems);
        gamePointWalletRepository.save(wallet);
        walletService.awardGems(userId, gems);
        return toResponse(wallet);
    }

    private GamePointWallet getOrCreateWallet(Long userId) {
        return gamePointWalletRepository.findById(userId)
                .orElseGet(() -> gamePointWalletRepository.save(GamePointWallet.builder()
                        .userId(userId)
                        .totalPoints(0)
                        .availablePoints(0)
                        .totalGemsConverted(0)
                        .build()));
    }

    private GamePointWalletResponse toResponse(GamePointWallet wallet) {
        return GamePointWalletResponse.builder()
                .userId(wallet.getUserId())
                .totalPoints(wallet.getTotalPoints())
                .availablePoints(wallet.getAvailablePoints())
                .totalGemsConverted(wallet.getTotalGemsConverted())
                .exchangeThreshold(EXCHANGE_THRESHOLD)
                .build();
    }
}
