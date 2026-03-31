package com.hdtpt.pentachat.friend.service;

import com.hdtpt.pentachat.friend.model.FriendRequest;
import com.hdtpt.pentachat.friend.repository.FriendRequestRepository;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho FriendService
 * 
 * Các test case:
 * - sendFriendRequest: Gửi yêu cầu kết bạn
 * - acceptFriend: Chấp nhận yêu cầu
 * - rejectFriend: Từ chối yêu cầu
 * - getPendingRequests: Lấy danh sách chờ
 * - areFriends: Kiểm tra quan hệ bạn bè
 */
@SpringBootTest
@Transactional
@DisplayName("Friend Service Tests")
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Xóa dữ liệu cũ
        friendRequestRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo test users
        user1 = User.builder()
                .username("user1_test")
                .password("password123")
                .build();

        user2 = User.builder()
                .username("user2_test")
                .password("password123")
                .build();

        user3 = User.builder()
                .username("user3_test")
                .password("password123")
                .build();

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);
    }

    @Test
    @DisplayName("Should send friend request successfully")
    void testSendFriendRequest_Success() {
        // When
        FriendRequest friendRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());

        // Then
        assertNotNull(friendRequest);
        assertNotNull(friendRequest.getId());
        assertEquals(user1.getId(), friendRequest.getFromUserId());
        assertEquals(user2.getId(), friendRequest.getToUserId());
        assertEquals("PENDING", friendRequest.getStatus());
        assertNotNull(friendRequest.getCreatedAt());
        assertNotNull(friendRequest.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw error when sending friend request to self")
    void testSendFriendRequest_ToSelf() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.sendFriendRequest(user1.getId(), user1.getId()));
        assertEquals("Cannot send friend request to yourself", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when from user does not exist")
    void testSendFriendRequest_FromUserNotFound() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.sendFriendRequest(999L, user2.getId()));
        assertEquals("From user does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when to user does not exist")
    void testSendFriendRequest_ToUserNotFound() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.sendFriendRequest(user1.getId(), 999L));
        assertEquals("To user does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when friend request already sent")
    void testSendFriendRequest_AlreadySent() {
        // Given
        friendService.sendFriendRequest(user1.getId(), user2.getId());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.sendFriendRequest(user1.getId(), user2.getId()));
        assertEquals("Friend request already sent", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject reverse pending request")
    void testSendFriendRequest_ReversePending() {
        friendService.sendFriendRequest(user1.getId(), user2.getId());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.sendFriendRequest(user2.getId(), user1.getId()));
        assertEquals("User already sent you a friend request", exception.getMessage());
    }

    @Test
    @DisplayName("Should re-send rejected friend request without creating duplicate")
    void testSendFriendRequest_ResendRejected() {
        FriendRequest request = friendService.sendFriendRequest(user1.getId(), user2.getId());
        friendService.rejectFriend(request.getId());

        FriendRequest resent = friendService.sendFriendRequest(user1.getId(), user2.getId());

        assertEquals(request.getId(), resent.getId());
        assertEquals("PENDING", resent.getStatus());
    }

    @Test
    @DisplayName("Should accept friend request successfully")
    void testAcceptFriend_Success() {
        // Given
        FriendRequest sentRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());

        // When
        FriendRequest acceptedRequest = friendService.acceptFriend(sentRequest.getId());

        // Then
        assertNotNull(acceptedRequest);
        assertEquals("ACCEPTED", acceptedRequest.getStatus());
        assertEquals(user1.getId(), acceptedRequest.getFromUserId());
        assertEquals(user2.getId(), acceptedRequest.getToUserId());
    }

    @Test
    @DisplayName("Should throw error when accepting non-existent request")
    void testAcceptFriend_RequestNotFound() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.acceptFriend(999L));
        assertEquals("Friend request not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when accepting already accepted request")
    void testAcceptFriend_AlreadyAccepted() {
        // Given
        FriendRequest sentRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());
        friendService.acceptFriend(sentRequest.getId());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.acceptFriend(sentRequest.getId()));
        assertEquals("Friend request is not in PENDING status", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject friend request successfully")
    void testRejectFriend_Success() {
        // Given
        FriendRequest sentRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());

        // When
        FriendRequest rejectedRequest = friendService.rejectFriend(sentRequest.getId());

        // Then
        assertNotNull(rejectedRequest);
        assertEquals("REJECTED", rejectedRequest.getStatus());
    }

    @Test
    @DisplayName("Should throw error when rejecting non-existent request")
    void testRejectFriend_RequestNotFound() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> friendService.rejectFriend(999L));
        assertEquals("Friend request not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should get pending requests for user")
    void testGetPendingRequests_Success() {
        // Given
        friendService.sendFriendRequest(user1.getId(), user2.getId());
        friendService.sendFriendRequest(user3.getId(), user2.getId());

        // When
        List<FriendRequest> pendingRequests = friendService.getPendingRequests(user2.getId());

        // Then
        assertNotNull(pendingRequests);
        assertEquals(2, pendingRequests.size());
        assertTrue(pendingRequests.stream()
                .allMatch(req -> req.getStatus().equals("PENDING")));
        assertTrue(pendingRequests.stream()
                .allMatch(req -> req.getToUserId().equals(user2.getId())));
    }

    @Test
    @DisplayName("Should get empty list when no pending requests")
    void testGetPendingRequests_Empty() {
        // When
        List<FriendRequest> pendingRequests = friendService.getPendingRequests(user2.getId());

        // Then
        assertNotNull(pendingRequests);
        assertTrue(pendingRequests.isEmpty());
    }

    @Test
    @DisplayName("Should check friendship status correctly")
    void testAreFriends_True() {
        // Given - Create two users that are friends
        FriendRequest sentRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());
        friendService.acceptFriend(sentRequest.getId());

        // When
        boolean areFriends = friendService.areFriends(user1.getId(), user2.getId());

        // Then
        assertTrue(areFriends);
    }

    @Test
    @DisplayName("Should check friendship status - not friends")
    void testAreFriends_False() {
        // When
        boolean areFriends = friendService.areFriends(user1.getId(), user2.getId());

        // Then
        assertFalse(areFriends);
    }

    @Test
    @DisplayName("Should check friendship status - pending request")
    void testAreFriends_PendingRequest() {
        // Given - Send request but don't accept
        friendService.sendFriendRequest(user1.getId(), user2.getId());

        // When
        boolean areFriends = friendService.areFriends(user1.getId(), user2.getId());

        // Then
        assertFalse(areFriends); // Only ACCEPTED requests count as friends
    }

    @Test
    @DisplayName("Should check friendship in both directions")
    void testAreFriends_BothDirections() {
        // Given
        FriendRequest sentRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());
        friendService.acceptFriend(sentRequest.getId());

        // When - Check both directions
        boolean areFriends1to2 = friendService.areFriends(user1.getId(), user2.getId());
        boolean areFriends2to1 = friendService.areFriends(user2.getId(), user1.getId());

        // Then
        assertTrue(areFriends1to2);
        assertTrue(areFriends2to1); // Friendship should work both ways
    }

    @Test
    @DisplayName("Full friend request workflow")
    void testFullFriendRequestWorkflow() {
        // Step 1: User1 sends friend request to User2
        FriendRequest sentRequest = friendService.sendFriendRequest(user1.getId(), user2.getId());
        assertEquals("PENDING", sentRequest.getStatus());

        // Step 2: User2 has pending requests
        List<FriendRequest> pendingRequests = friendService.getPendingRequests(user2.getId());
        assertEquals(1, pendingRequests.size());

        // Step 3: They are not yet friends
        assertFalse(friendService.areFriends(user1.getId(), user2.getId()));

        // Step 4: User2 accepts the request
        FriendRequest acceptedRequest = friendService.acceptFriend(sentRequest.getId());
        assertEquals("ACCEPTED", acceptedRequest.getStatus());

        // Step 5: Now they are friends
        assertTrue(friendService.areFriends(user1.getId(), user2.getId()));

        // Step 6: No more pending requests
        List<FriendRequest> emptyRequests = friendService.getPendingRequests(user2.getId());
        assertTrue(emptyRequests.isEmpty());
    }
}
