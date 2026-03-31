package com.hdtpt.pentachat.games.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_point_wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePointWallet {

    @Id
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer totalPoints;

    @Column(nullable = false)
    private Integer availablePoints;

    @Column(nullable = false)
    private Integer totalGemsConverted;
}
