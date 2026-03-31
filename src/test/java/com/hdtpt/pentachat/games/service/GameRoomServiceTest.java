package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Game Room Service Tests")
class GameRoomServiceTest {

    @Autowired
    private GameRoomService gameRoomService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Should start and end game successfully")
    void testGameWorkflow() {
        Long roomId = 1L;
        Room room = gameRoomService.getRoom(roomId);
        assertEquals("WAITING", room.getStatus());

        gameRoomService.startGame(roomId);
        assertEquals("PLAYING", room.getStatus());

        gameRoomService.endGame(roomId);
        assertEquals("ENDED", room.getStatus());
    }

    @Test
    @DisplayName("Should throw error if room not found")
    void testRoomNotFound() {
        assertThrows(RuntimeException.class, () -> gameRoomService.getRoom(999L));
    }
}
