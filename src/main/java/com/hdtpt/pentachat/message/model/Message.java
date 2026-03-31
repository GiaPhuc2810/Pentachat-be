package com.hdtpt.pentachat.message.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.hdtpt.pentachat.util.BaseEntity;

/**
 * Message entity supporting both personal and group messages
 * Type field indicates: PERSONAL (1-to-1) or GROUP (1-to-many)
 * TargetId: userId for PERSONAL, groupId for GROUP
 */
@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    public enum MessageType {
        PERSONAL, // 1-to-1 message
        GROUP // Group message
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(nullable = false)
    private Long targetId; // userId for PERSONAL, groupId for GROUP

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(nullable = true)
    private Boolean isRead;

    // Legacy field for backward compatibility
    @Column
    private Long toUserId;
}
