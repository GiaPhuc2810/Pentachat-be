package com.hdtpt.pentachat.identity.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import com.hdtpt.pentachat.util.BaseEntity;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column(nullable = false)
    private Long userId;

    private LocalDateTime expiryDate;

    // Hàm kiểm tra xem token còn hạn không
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}