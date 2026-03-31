package com.hdtpt.pentachat.games.service;

import org.springframework.stereotype.Service;

import com.hdtpt.pentachat.games.model.Room;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameRoomService {

    // Giả lập bảng rooms (sau này thay bằng DB)
    private final Map<Long, Room> rooms = new HashMap<>();

    public GameRoomService() {
        rooms.put(1L, new Room(1L, "WAITING"));
        rooms.put(2L, new Room(2L, "WAITING"));
    }

    // START GAME
    public void startGame(Long roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room không tồn tại");
        }

        if (!"WAITING".equals(room.getStatus())) {
            throw new RuntimeException("Không thể start game. Trạng thái hiện tại: " + room.getStatus());
        }

        room.setStatus("PLAYING");
    }

    // END GAME
    public void endGame(Long roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room không tồn tại");
        }

        if (!"PLAYING".equals(room.getStatus())) {
            throw new RuntimeException("Không thể end game. Trạng thái hiện tại: " + room.getStatus());
        }

        room.setStatus("ENDED");
    }

    public Room getRoom(Long roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room khÃ´ng tá»“n táº¡i");
        }
        return room;
    }
}
