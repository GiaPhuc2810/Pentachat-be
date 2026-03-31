package com.hdtpt.pentachat.gate;

import com.hdtpt.pentachat.message.dto.MessageDTO;

public class ConnectionGuard {

    public static void check(MessageDTO message) {
        if (message == null || message.getFrom() == null) {
            throw new RuntimeException("Missing userId - connection terminated");
        }
    }
}
