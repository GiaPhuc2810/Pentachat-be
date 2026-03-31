package com.hdtpt.pentachat.call.audio.service;

import com.hdtpt.pentachat.call.audio.dto.CallSignalMessage;
import com.hdtpt.pentachat.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class AudioCallService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "ready",
            "accept",
            "reject",
            "offer",
            "answer",
            "ice",
            "hangup",
            "end",
            "endcall",
            "end_call"
    );

    private static final Set<String> TERMINATION_TYPES = Set.of("hangup", "reject", "end", "endcall", "end_call");
    private static final Map<String, String> TYPE_ALIASES = Map.of(
            "end", "hangup",
            "endcall", "hangup",
            "end_call", "hangup"
    );

    private final SimpMessagingTemplate messagingTemplate;

    public AudioCallService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void relaySignal(CallSignalMessage message) {
        validate(message);
        String destination = buildDestination(message.getToUserId());
        messagingTemplate.convertAndSend(destination, message);

        if (isTerminationSignal(message.getType())) {
            // Mirror termination to sender topic so either side can close call state immediately.
            String senderDestination = buildDestination(message.getFromUserId());
            messagingTemplate.convertAndSend(senderDestination, message);
            log.info("Relayed termination signal [{}] from {} to {} and self-topic", message.getType(),
                    message.getFromUserId(), message.getToUserId());
            return;
        }

        log.info("Relayed audio call signal [{}] from {} to {}", message.getType(), message.getFromUserId(),
                message.getToUserId());
    }

    public String buildDestination(Long toUserId) {
        return "/topic/call.audio." + toUserId;
    }

    private void validate(CallSignalMessage message) {
        if (message == null) {
            throw new AppException("Call message is required", HttpStatus.BAD_REQUEST);
        }
        if (message.getFromUserId() == null || message.getToUserId() == null) {
            throw new AppException("fromUserId and toUserId are required", HttpStatus.BAD_REQUEST);
        }
        if (message.getFromUserId().equals(message.getToUserId())) {
            throw new AppException("fromUserId cannot equal toUserId", HttpStatus.BAD_REQUEST);
        }
        if (message.getType() == null || message.getType().trim().isEmpty()) {
            throw new AppException("type is required", HttpStatus.BAD_REQUEST);
        }
        String normalized = message.getType().trim().toLowerCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new AppException("Invalid call signal type", HttpStatus.BAD_REQUEST);
        }
        message.setType(TYPE_ALIASES.getOrDefault(normalized, normalized));
    }

    private boolean isTerminationSignal(String type) {
        return TERMINATION_TYPES.contains(type);
    }
}
