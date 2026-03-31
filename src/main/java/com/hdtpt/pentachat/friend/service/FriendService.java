package com.hdtpt.pentachat.friend.service;

import com.hdtpt.pentachat.friend.model.FriendRequest;
import com.hdtpt.pentachat.friend.repository.FriendRequestRepository;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Friend Service - Xử lý logic quản lý quan hệ bạn bè
 * 
 * Chứa các hàm:
 * - sendFriendRequest(fromId, toId): Gửi yêu cầu kết bạn
 * - sendFriendRequestByUsername(fromId, toUsername): Gửi yêu cầu kết bạn qua
 * username
 * - acceptFriend(requestId): Chấp nhận yêu cầu kết bạn
 * - rejectFriend(requestId): Từ chối yêu cầu kết bạn
 * - getPendingRequests(userId): Lấy danh sách yêu cầu đang chờ
 * - areFriends(userId1, userId2): Kiểm tra xem hai user có phải bạn bè không
 * - getFriendsList(userId): Lấy danh sách bạn bè đã kết bạn
 */
@Service
@Slf4j
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRequestRepository friendRequestRepository,
            UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gửi yêu cầu kết bạn
     * 
     * @param fromId User ID của người gửi
     * @param toId   User ID của người nhận
     * @return FriendRequest - Yêu cầu kết bạn vừa được tạo
     */
    public FriendRequest sendFriendRequest(Long fromId, Long toId) {
        try {
            // Validate input
            if (fromId == null) {
                throw new IllegalArgumentException("fromId cannot be null");
            }
            if (toId == null) {
                throw new IllegalArgumentException("toId cannot be null");
            }
            if (fromId.equals(toId)) {
                throw new IllegalArgumentException("Cannot send friend request to yourself");
            }

            // Kiểm tra xem cả hai user có tồn tại không
            Optional<User> fromUser = userRepository.findById(fromId);
            Optional<User> toUser = userRepository.findById(toId);

            if (fromUser.isEmpty()) {
                throw new IllegalArgumentException("From user does not exist");
            }
            if (toUser.isEmpty()) {
                throw new IllegalArgumentException("To user does not exist");
            }

            // Kiểm tra xem họ đã là bạn bè chưa
            if (friendRequestRepository.areFriends(fromId, toId)) {
                throw new IllegalArgumentException("Users are already friends");
            }

            // Kiểm tra xem yêu cầu đã tồn tại chưa
            Optional<FriendRequest> existingRequest = friendRequestRepository.findByFromUserIdAndToUserId(fromId, toId);

            if (existingRequest.isPresent()) {
                FriendRequest req = existingRequest.get();
                if (req.getStatus().equals("PENDING")) {
                    throw new IllegalArgumentException("Friend request already sent");
                } else if (req.getStatus().equals("ACCEPTED")) {
                    throw new IllegalArgumentException("Users are already friends");
                } else if (req.getStatus().equals("REJECTED")) {
                    req.setStatus("PENDING");
                    FriendRequest resent = friendRequestRepository.save(req);
                    log.info("Friend request re-sent from {} to {}", fromId, toId);
                    return resent;
                }
            }

            Optional<FriendRequest> reverseRequest = friendRequestRepository.findByFromUserIdAndToUserId(toId, fromId);
            if (reverseRequest.isPresent()) {
                FriendRequest req = reverseRequest.get();
                if (req.getStatus().equals("PENDING")) {
                    throw new IllegalArgumentException("User already sent you a friend request");
                } else if (req.getStatus().equals("ACCEPTED")) {
                    throw new IllegalArgumentException("Users are already friends");
                }
            }

            // Tạo yêu cầu kết bạn mới
            FriendRequest friendRequest = FriendRequest.builder()
                    .fromUserId(fromId)
                    .toUserId(toId)
                    .status("PENDING")
                    .build();

            FriendRequest saved = friendRequestRepository.save(friendRequest);
            log.info("Friend request sent from {} to {}", fromId, toId);
            return saved;

        } catch (Exception e) {
            log.error("Error sending friend request: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Gửi yêu cầu kết bạn bằng Username
     *
     * @param fromId     User ID của người gửi
     * @param toUsername Username của người nhận
     * @return FriendRequest
     */
    public FriendRequest sendFriendRequestByUsername(Long fromId, String toUsername) {
        User toUser = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng '" + toUsername + "' không tồn tại"));
        return sendFriendRequest(fromId, toUser.getId());
    }

    /**
     * Chấp nhận yêu cầu kết bạn
     * 
     * @param requestId ID của yêu cầu kết bạn
     * @return FriendRequest - Yêu cầu kết bạn vừa được chấp nhận
     */
    public FriendRequest acceptFriend(Long requestId) {
        try {
            // Validate input
            if (requestId == null) {
                throw new IllegalArgumentException("requestId cannot be null");
            }

            // Tìm yêu cầu kết bạn
            Optional<FriendRequest> optionalRequest = friendRequestRepository.findById(requestId);
            if (optionalRequest.isEmpty()) {
                throw new IllegalArgumentException("Friend request not found");
            }

            FriendRequest friendRequest = optionalRequest.get();

            // Kiểm tra xem yêu cầu đang ở trạng thái PENDING
            if (!friendRequest.getStatus().equals("PENDING")) {
                throw new IllegalArgumentException("Friend request is not in PENDING status");
            }

            // Cập nhật trạng thái thành ACCEPTED
            friendRequest.setStatus("ACCEPTED");

            FriendRequest saved = friendRequestRepository.save(friendRequest);
            log.info("Friend request {} accepted", requestId);
            return saved;

        } catch (Exception e) {
            log.error("Error accepting friend request: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Từ chối yêu cầu kết bạn
     * 
     * @param requestId ID của yêu cầu kết bạn
     * @return FriendRequest - Yêu cầu kết bạn vừa được từ chối
     */
    public FriendRequest rejectFriend(Long requestId) {
        try {
            // Validate input
            if (requestId == null) {
                throw new IllegalArgumentException("requestId cannot be null");
            }

            // Tìm yêu cầu kết bạn
            Optional<FriendRequest> optionalRequest = friendRequestRepository.findById(requestId);
            if (optionalRequest.isEmpty()) {
                throw new IllegalArgumentException("Friend request not found");
            }

            FriendRequest friendRequest = optionalRequest.get();

            // Kiểm tra xem yêu cầu đang ở trạng thái PENDING
            if (!friendRequest.getStatus().equals("PENDING")) {
                throw new IllegalArgumentException("Friend request is not in PENDING status");
            }

            // Cập nhật trạng thái thành REJECTED
            friendRequest.setStatus("REJECTED");

            FriendRequest saved = friendRequestRepository.save(friendRequest);
            log.info("Friend request {} rejected", requestId);
            return saved;

        } catch (Exception e) {
            log.error("Error rejecting friend request: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Lấy danh sách yêu cầu kết bạn đang chờ của user
     * 
     * @param userId User ID
     * @return List<FriendRequest> - Danh sách yêu cầu đang chờ
     */
    public List<FriendRequest> getPendingRequests(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }

            List<FriendRequest> requests = friendRequestRepository.findByToUserIdAndStatus(userId, "PENDING");
            log.info("Retrieved {} pending requests for user {}", requests.size(), userId);
            return requests;

        } catch (Exception e) {
            log.error("Error getting pending requests: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Kiểm tra xem hai user có phải bạn bè không
     * 
     * @param userId1 User ID 1
     * @param userId2 User ID 2
     * @return boolean - true nếu là bạn bè, false nếu không
     */
    public boolean areFriends(Long userId1, Long userId2) {
        try {
            if (userId1 == null) {
                throw new IllegalArgumentException("userId1 cannot be null");
            }
            if (userId2 == null) {
                throw new IllegalArgumentException("userId2 cannot be null");
            }

            return friendRequestRepository.areFriends(userId1, userId2);

        } catch (Exception e) {
            log.error("Error checking friendship: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Lấy danh sách bạn bè của user
     * 
     * @param userId User ID
     * @return List<FriendRequest> - Danh sách các quan hệ bạn bè đã ACCEPTED
     */
    public List<FriendRequest> getFriendsList(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }
            return friendRequestRepository.findAllFriends(userId);
        } catch (Exception e) {
            log.error("Error getting friends list: {}", e.getMessage());
            throw e;
        }
    }
}
