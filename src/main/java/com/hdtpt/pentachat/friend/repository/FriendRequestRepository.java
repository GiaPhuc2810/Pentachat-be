package com.hdtpt.pentachat.friend.repository;

import com.hdtpt.pentachat.friend.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    /**
     * Tìm yêu cầu kết bạn từ fromUserId đến toUserId
     */
    Optional<FriendRequest> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    /**
     * Lấy tất cả yêu cầu kết bạn đang chờ của user
     */
    List<FriendRequest> findByToUserIdAndStatus(Long toUserId, String status);

    /**
     * Lấy tất cả yêu cầu kết bạn đã gửi của user
     */
    List<FriendRequest> findByFromUserIdAndStatus(Long fromUserId, String status);

    /**
     * Kiểm tra xem hai user có phải bạn bè không
     */
    @Query("SELECT CASE WHEN COUNT(fr) > 0 THEN true ELSE false END " +
            "FROM FriendRequest fr WHERE " +
            "((fr.fromUserId = :userId1 AND fr.toUserId = :userId2) OR " +
            "(fr.fromUserId = :userId2 AND fr.toUserId = :userId1)) AND " +
            "fr.status = 'ACCEPTED'")
    boolean areFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "(fr.fromUserId = :userId OR fr.toUserId = :userId) AND " +
            "fr.status = 'ACCEPTED'")
    List<FriendRequest> findAllFriends(@Param("userId") Long userId);
}
