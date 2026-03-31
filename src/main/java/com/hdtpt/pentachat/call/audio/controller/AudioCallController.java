package com.hdtpt.pentachat.call.audio.controller;

import com.hdtpt.pentachat.call.audio.dto.CallSignalMessage;
import com.hdtpt.pentachat.call.audio.service.AudioCallService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class AudioCallController {

    private final AudioCallService audioCallService;

    public AudioCallController(AudioCallService audioCallService) {
        this.audioCallService = audioCallService;
    }

    @MessageMapping("/call.audio")
    public void handleSignal(@Valid CallSignalMessage message) {
        log.info("Audio call signal received: type={}, from={}, to={}", message.getType(), message.getFromUserId(), message.getToUserId());
        audioCallService.relaySignal(message);
    }
}
