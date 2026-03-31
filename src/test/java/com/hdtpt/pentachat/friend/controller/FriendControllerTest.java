package com.hdtpt.pentachat.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdtpt.pentachat.friend.dto.FriendRequestDTO;
import com.hdtpt.pentachat.friend.model.FriendRequest;
import com.hdtpt.pentachat.friend.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho FriendController
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Friend Controller Tests")
class FriendControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private FriendService friendService;

        @MockitoBean
        private JavaMailSender mailSender;

        private FriendRequest mockFriendRequest;
        private FriendRequestDTO mockDTO;

        @BeforeEach
        void setUp() {
                Long requestId = 100L;
                Long userId1 = 1L;
                Long userId2 = 2L;

                mockFriendRequest = FriendRequest.builder()
                                .id(requestId)
                                .fromUserId(userId1)
                                .toUserId(userId2)
                                .status("PENDING")
                                .build();

                mockDTO = FriendRequestDTO.builder()
                                .id(requestId)
                                .fromUserId(userId1)
                                .toUserId(userId2)
                                .status("PENDING")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        @Test
        @DisplayName("POST /api/friends/request - Send friend request successfully")
        void testSendFriendRequest_Success() throws Exception {
                // Given
                FriendRequestDTO requestDTO = FriendRequestDTO.builder()
                                .fromUserId(mockDTO.getFromUserId())
                                .toUserId(mockDTO.getToUserId())
                                .build();

                when(friendService.sendFriendRequest(anyLong(), anyLong()))
                                .thenReturn(mockFriendRequest);

                // When & Then
                mockMvc.perform(post("/api/friends/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", containsString("Friend request sent successfully")))
                                .andExpect(jsonPath("$.data.id", notNullValue()))
                                .andExpect(jsonPath("$.data.status", is("PENDING")));

                verify(friendService, times(1)).sendFriendRequest(anyLong(), anyLong());
        }

        @Test
        @DisplayName("POST /api/friends/request - Invalid request (missing fields)")
        void testSendFriendRequest_InvalidRequest() throws Exception {
                // Given
                FriendRequestDTO invalidDTO = FriendRequestDTO.builder()
                                .fromUserId(null) // Null instead of Empty string
                                .toUserId(mockDTO.getToUserId())
                                .build();

                // When & Then
                mockMvc.perform(post("/api/friends/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/friends/request - Friend request already sent")
        void testSendFriendRequest_AlreadySent() throws Exception {
                // Given
                FriendRequestDTO requestDTO = FriendRequestDTO.builder()
                                .fromUserId(mockDTO.getFromUserId())
                                .toUserId(mockDTO.getToUserId())
                                .build();

                when(friendService.sendFriendRequest(anyLong(), anyLong()))
                                .thenThrow(new IllegalArgumentException("Friend request already sent"));

                // When & Then
                mockMvc.perform(post("/api/friends/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", containsString("Friend request already sent")));
        }

        @Test
        @DisplayName("POST /api/friends/accept/{requestId} - Accept friend request successfully")
        void testAcceptFriend_Success() throws Exception {
                // Given
                FriendRequest acceptedRequest = FriendRequest.builder()
                                .id(mockFriendRequest.getId())
                                .fromUserId(mockFriendRequest.getFromUserId())
                                .toUserId(mockFriendRequest.getToUserId())
                                .status("ACCEPTED")
                                .build();

                when(friendService.acceptFriend(mockFriendRequest.getId()))
                                .thenReturn(acceptedRequest);

                // When & Then
                mockMvc.perform(post("/api/friends/accept/{requestId}", mockFriendRequest.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message",
                                                containsString("Friend request accepted successfully")))
                                .andExpect(jsonPath("$.data.status", is("ACCEPTED")));

                verify(friendService, times(1)).acceptFriend(mockFriendRequest.getId());
        }

        @Test
        @DisplayName("POST /api/friends/accept/{requestId} - Request not found")
        void testAcceptFriend_NotFound() throws Exception {
                // Given
                Long invalidId = 999L;
                when(friendService.acceptFriend(invalidId))
                                .thenThrow(new IllegalArgumentException("Friend request not found"));

                // When & Then
                mockMvc.perform(post("/api/friends/accept/{requestId}", invalidId))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", containsString("Friend request not found")));
        }

        @Test
        @DisplayName("POST /api/friends/reject/{requestId} - Reject friend request successfully")
        void testRejectFriend_Success() throws Exception {
                // Given
                FriendRequest rejectedRequest = FriendRequest.builder()
                                .id(mockFriendRequest.getId())
                                .fromUserId(mockFriendRequest.getFromUserId())
                                .toUserId(mockFriendRequest.getToUserId())
                                .status("REJECTED")
                                .build();

                when(friendService.rejectFriend(mockFriendRequest.getId()))
                                .thenReturn(rejectedRequest);

                // When & Then
                mockMvc.perform(post("/api/friends/reject/{requestId}", mockFriendRequest.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message",
                                                containsString("Friend request rejected successfully")))
                                .andExpect(jsonPath("$.data.status", is("REJECTED")));

                verify(friendService, times(1)).rejectFriend(mockFriendRequest.getId());
        }

        @Test
        @DisplayName("GET /api/friends/pending/{userId} - Get pending requests successfully")
        void testGetPendingRequests_Success() throws Exception {
                // Given
                Long userId = mockDTO.getToUserId();
                List<FriendRequest> pendingRequests = Arrays.asList(mockFriendRequest);

                when(friendService.getPendingRequests(userId))
                                .thenReturn(pendingRequests);

                // When & Then
                mockMvc.perform(get("/api/friends/pending/{userId}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message",
                                                containsString("Pending requests retrieved successfully")))
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].status", is("PENDING")));

                verify(friendService, times(1)).getPendingRequests(userId);
        }

        @Test
        @DisplayName("GET /api/friends/pending/{userId} - Empty pending requests")
        void testGetPendingRequests_Empty() throws Exception {
                // Given
                Long userId = 99L;
                when(friendService.getPendingRequests(userId))
                                .thenReturn(Arrays.asList());

                // When & Then
                mockMvc.perform(get("/api/friends/pending/{userId}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/friends/check/{userId1}/{userId2} - Check friendship - are friends")
        void testCheckFriendship_AreFriends() throws Exception {
                // Given
                Long userId1 = mockDTO.getFromUserId();
                Long userId2 = mockDTO.getToUserId();

                when(friendService.areFriends(userId1, userId2))
                                .thenReturn(true);

                // When & Then
                mockMvc.perform(get("/api/friends/check/{userId1}/{userId2}", userId1, userId2))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message",
                                                containsString("Friendship status retrieved successfully")))
                                .andExpect(jsonPath("$.data", is(true)));

                verify(friendService, times(1)).areFriends(userId1, userId2);
        }

        @Test
        @DisplayName("GET /api/friends/check/{userId1}/{userId2} - Check friendship - not friends")
        void testCheckFriendship_NotFriends() throws Exception {
                // Given
                Long userId1 = 5L;
                Long userId2 = 6L;

                when(friendService.areFriends(userId1, userId2))
                                .thenReturn(false);

                // When & Then
                mockMvc.perform(get("/api/friends/check/{userId1}/{userId2}", userId1, userId2))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data", is(false)));
        }
}
