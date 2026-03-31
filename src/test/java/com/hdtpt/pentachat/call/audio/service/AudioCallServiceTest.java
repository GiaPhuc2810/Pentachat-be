package com.hdtpt.pentachat.call.audio.service;

import com.hdtpt.pentachat.call.audio.dto.CallSignalMessage;
import com.hdtpt.pentachat.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DisplayName("Audio Call Service Tests")
class AudioCallServiceTest {

    @Autowired
    private AudioCallService audioCallService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("Should relay call signal to target topic")
    void testRelaySignal() {
        CallSignalMessage message = CallSignalMessage.builder()
                .fromUserId(1L)
                .toUserId(2L)
                .type("ready")
                .sdp("v=0")
                .build();

        audioCallService.relaySignal(message);

        String destination = audioCallService.buildDestination(2L);
        verify(messagingTemplate).convertAndSend(destination, message);
        assertEquals("ready", message.getType());
    }

    @Test
    @DisplayName("Should reject invalid call signal type")
    void testRejectInvalidType() {
        CallSignalMessage message = CallSignalMessage.builder()
                .fromUserId(1L)
                .toUserId(2L)
                .type("invalid")
                .build();

        AppException ex = assertThrows(AppException.class, () -> audioCallService.relaySignal(message));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should normalize endCall to hangup and relay to both participants")
    void testNormalizeEndCallAndRelayToBothSides() {
        CallSignalMessage message = CallSignalMessage.builder()
                .fromUserId(2L)
                .toUserId(1L)
                .type("endCall")
                .build();

        audioCallService.relaySignal(message);

        verify(messagingTemplate, times(1)).convertAndSend(audioCallService.buildDestination(1L), message);
        verify(messagingTemplate, times(1)).convertAndSend(audioCallService.buildDestination(2L), message);
        assertEquals("hangup", message.getType());
    }
}
