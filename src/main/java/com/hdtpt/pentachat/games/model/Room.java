package com.hdtpt.pentachat.games.model;

public class Room {

    private Long id;
    private String status; // WAITING, PLAYING, ENDED

    public Room() {
    }

    public Room(Long id, String status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
