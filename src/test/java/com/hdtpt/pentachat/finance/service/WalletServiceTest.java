package com.hdtpt.pentachat.finance.service;

import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.finance.model.Wallet;
import com.hdtpt.pentachat.finance.repository.WalletRepository;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Wallet Service Tests")
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        user1 = User.builder().username("user1").password("pass").build();
        user2 = User.builder().username("user2").password("pass").build();
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        walletService.createWallet(user1.getId(), 100.0);
        walletService.createWallet(user2.getId(), 50.0);
    }

    @Test
    @DisplayName("Should get correct balance")
    void testGetBalance() {
        Wallet wallet = walletService.getBalance(user1.getId());
        assertEquals(100.0, wallet.getBalance());
    }

    @Test
    @DisplayName("Should deposit successfully")
    void testDeposit() {
        walletService.deposit(user1.getId(), 50.0);
        Wallet wallet = walletService.getBalance(user1.getId());
        assertEquals(150.0, wallet.getBalance());
    }

    @Test
    @DisplayName("Should withdraw successfully")
    void testWithdraw() {
        walletService.withdraw(user1.getId(), 30.0);
        Wallet wallet = walletService.getBalance(user1.getId());
        assertEquals(70.0, wallet.getBalance());
    }

    @Test
    @DisplayName("Should throw error on insufficient balance")
    void testWithdraw_InsufficientBalance() {
        assertThrows(AppException.class, () -> {
            walletService.withdraw(user1.getId(), 200.0);
        });
    }

    @Test
    @DisplayName("Should transfer successfully")
    void testTransfer() {
        walletService.transfer(user1.getId(), "user2", 40.0);

        assertEquals(60.0, walletService.getBalance(user1.getId()).getBalance());
        assertEquals(90.0, walletService.getBalance(user2.getId()).getBalance());
    }
}
