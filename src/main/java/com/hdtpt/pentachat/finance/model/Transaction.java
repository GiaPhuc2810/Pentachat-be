package com.hdtpt.pentachat.finance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.hdtpt.pentachat.util.BaseEntity;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private Long fromUserId;
    private Long toUserId;

    @Column(nullable = false)
    private Double amount;

    public enum TransactionType {
        DEPOSIT, WITHDRAW, TRANSFER
    }
}