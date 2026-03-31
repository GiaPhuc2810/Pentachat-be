package com.hdtpt.pentachat.games.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hdtpt.pentachat.util.BaseEntity;

/**
 * Thực thể lưu trữ lời mời vào phòng chơi game
 */
@Entity
@Table(name = "room_invites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInvite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long inviterId; // ID người gửi lời mời

    @Column(nullable = false)
    private Long inviteeId; // ID người nhận lời mời

    @Enumerated(EnumType.STRING)
    private InviteStatus status; // Trạng thái: PENDING, ACCEPTED, REJECTED
}
