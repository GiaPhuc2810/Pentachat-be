package com.hdtpt.pentachat.games.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hdtpt.pentachat.games.model.Room;
import com.hdtpt.pentachat.games.service.GameRoomService;

@Controller
@RequestMapping("/api/game")
public class RoomViewController {
    private final GameRoomService roomService;

    public RoomViewController(GameRoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/start/{roomId}")
    public String startGame(@PathVariable Long roomId, Model model) {
        roomService.startGame(roomId);
        Room room = roomService.getRoom(roomId);
        model.addAttribute("room", room);
        return "game-status";
    }

    @GetMapping("/end/{roomId}")
    public String endGame(@PathVariable Long roomId, Model model) {
        roomService.endGame(roomId);
        Room room = roomService.getRoom(roomId);
        model.addAttribute("room", room);
        return "game-status";
    }
}
