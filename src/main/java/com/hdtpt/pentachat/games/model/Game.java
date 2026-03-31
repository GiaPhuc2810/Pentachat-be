package com.hdtpt.pentachat.games.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.hdtpt.pentachat.util.BaseEntity;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- SỬA Ở ĐÂY: Thêm columnDefinition = "nvarchar(255)" ---
    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    // --- SỬA Ở ĐÂY: Thêm columnDefinition = "nvarchar(MAX)" ---
    @Column(columnDefinition = "nvarchar(MAX)")
    private String description;

    private String imageUrl; // Link ảnh thì không cần tiếng Việt nên giữ nguyên

}