package com.hdtpt.pentachat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectGaugeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectGaugeApplication.class, args);
    }

    // // --- ĐOẠN CODE TỰ ĐỘNG TẠO GAME MINI ---
    // @Bean
    // CommandLineRunner initData(GameService gameService) {
    // return args -> {
    // gameService.createDummyGames(); // Tự động tạo Cờ Caro, Rắn săn mồi...
    // };
    // }
}