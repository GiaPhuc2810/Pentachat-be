package com.hdtpt.pentachat.friend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.hdtpt.pentachat.util.BaseEntity;

/**
 * FriendRequest Entity - Quản lý yêu cầu kết bạn
 * 
 * Status: PENDING (chờ chấp nhận), ACCEPTED (đã chấp nhận), REJECTED (bị từ
 * chối)
 */
@Entity
@Table(name = "friend_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromUserId;

    @Column(nullable = false)
    private Long toUserId;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, REJECTED
}
