package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.model.InviteStatus;
import com.hdtpt.pentachat.games.model.RoomInvite;
import com.hdtpt.pentachat.games.repository.RoomInviteRepository;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Room Service Tests")
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomInviteRepository inviteRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private JavaMailSender mailSender;

    private User inviter;
    private User invitee;

    @BeforeEach
    void setUp() {
        inviteRepository.deleteAll();
        userRepository.deleteAll();

        inviter = userRepository.save(User.builder().username("inviter").password("pass").build());
        invitee = userRepository.save(User.builder().username("invitee").password("pass").build());
    }

    @Test
    @DisplayName("Should invite user successfully")
    void testInviteUser() {
        RoomInvite invite = roomService.inviteUser(101L, inviter.getId(), invitee.getId());

        assertNotNull(invite);
        assertEquals(InviteStatus.PENDING, invite.getStatus());
        assertEquals(101L, invite.getRoomId());
    }

    @Test
    @DisplayName("Should accept invite successfully")
    void testAcceptInvite() {
        RoomInvite invite = roomService.inviteUser(101L, inviter.getId(), invitee.getId());
        roomService.acceptInvite(invite.getId());

        RoomInvite updated = inviteRepository.findById(invite.getId()).orElseThrow();
        assertEquals(InviteStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    @DisplayName("Should get room members correctly")
    void testGetRoomMembers() {
        RoomInvite invite = roomService.inviteUser(101L, inviter.getId(), invitee.getId());
        roomService.acceptInvite(invite.getId());

        Map<String, Object> membersInfo = roomService.getRoomMembers(101L);
        assertEquals("inviter", membersInfo.get("owner"));
        @SuppressWarnings("unchecked")
        List<String> members = (List<String>) membersInfo.get("members");
        assertTrue(members.contains("inviter"));
        assertTrue(members.contains("invitee"));
    }
}
