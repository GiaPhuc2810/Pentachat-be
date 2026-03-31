package com.hdtpt.pentachat.message.service;

import com.hdtpt.pentachat.message.repository.MessageRepository;
import com.hdtpt.pentachat.message.dto.response.MessageResponse;
import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.identity.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Message Service - Xử lý logic gửi/nhận messages
 * 
 * Chứa hàm pushToUser() để gửi tin nhắn cho user đích
 * Lưu messages trong database thông qua DataApi
 */
@Service
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    /**
     * Hàm pushToUser - Gửi tin nhắn cho user đích
     * 
     * @param fromUserId User ID của người gửi
     * @param toUserId   User ID của người nhận
     * @param content    Nội dung tin nhắn
     * @return MessageResponse - Tin nhắn vừa được gửi
     */
    public MessageResponse pushToUser(Long fromUserId, Long toUserId, String content) {
        try {
            // Validate
            if (fromUserId == null) {
                throw new IllegalArgumentException("fromUserId cannot be null");
            }
            if (toUserId == null) {
                throw new IllegalArgumentException("toUserId cannot be null");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content cannot be empty");
            }

            // Tạo message mới trong database
            Message message = Message.builder()
                    .fromUserId(fromUserId)
                    .toUserId(toUserId)
                    .targetId(toUserId) // Fix: Set targetId for PERSONAL messages
                    .content(content)
                    .type(Message.MessageType.PERSONAL)
                    .build();
            message = messageRepository.save(message);

            // Convert to MessageResponse
            MessageResponse response = MessageResponse.builder()
                    .id(message.getId())
                    .fromId(message.getFromUserId())
                    .toId(message.getToUserId())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .isRead(message.getIsRead())
                    .build();

            log.info("Message pushed to user {} from user {}: {}", toUserId, fromUserId, content);

            // Notify BOTH users (sender and recipient) for real-time UI updates
            notifyUserNewMessage(toUserId, response);
            notifyUserNewMessage(fromUserId, response);

            return response;

        } catch (Exception e) {
            log.error("Error pushing message to user {}: {}", toUserId, e.getMessage());
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Thông báo cho user có tin nhắn mới
     * (Đây là nơi bạn có thể integrate WebSocket để push real-time)
     */
    private void notifyUserNewMessage(Long userId, MessageResponse message) {
        log.info("🔔 NOTIFICATION: User {} has new message from {} - '{}'",
                userId, message.getFromId(), message.getContent());

        try {
            // Gửi qua WebSocket topic mà user đang subscribe
            // Topic format: /topic/messages/{userId} theo cấu hình frontend
            messagingTemplate.convertAndSend("/topic/messages/" + userId, message);
            log.info("WS: Message sent to /topic/messages/{}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification: {}", e.getMessage());
        }
    }

    /**
     * Lấy inbox của user
     */
    public List<MessageResponse> getUserInbox(Long userId) {
        List<Message> messages = messageRepository.findByToUserId(userId);
        return convertToResponseList(messages);
    }

    /**
     * Lấy các tin nhắn giữa 2 user (conversation)
     */
    public List<MessageResponse> getConversation(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(userId1, userId2,
                userId1, userId2);
        return convertToResponseList(messages);
    }

    /**
     * Đánh dấu message là đã đọc
     */
    public void markAsRead(Long userId, Long messageId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (messageId == null) {
            throw new IllegalArgumentException("messageId cannot be null");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!Objects.equals(message.getToUserId(), userId)) {
            throw new IllegalArgumentException("User is not allowed to mark this message as read");
        }

        message.setIsRead(true);
        messageRepository.save(message);
    }

    /**
     * Xóa message
     */
    public void deleteMessage(Long userId, Long messageId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (messageId == null) {
            throw new IllegalArgumentException("messageId cannot be null");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        boolean isSender = Objects.equals(message.getFromUserId(), userId);
        boolean isRecipient = Objects.equals(message.getToUserId(), userId);
        if (!isSender && !isRecipient) {
            throw new IllegalArgumentException("User is not allowed to delete this message");
        }

        messageRepository.delete(message);
    }

    /**
     * Helper method: Convert List<Message> to List<MessageResponse>
     */
    private List<MessageResponse> convertToResponseList(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = new HashSet<>();
        for (Message message : messages) {
            if (message.getFromUserId() != null) {
                userIds.add(message.getFromUserId());
            }
            if (message.getToUserId() != null) {
                userIds.add(message.getToUserId());
            } else if (message.getTargetId() != null && message.getType() == Message.MessageType.PERSONAL) {
                userIds.add(message.getTargetId());
            }
        }

        Map<Long, String> usernamesById = new HashMap<>();
        userRepository.findAllById(userIds)
                .forEach(user -> usernamesById.put(user.getId(), user.getUsername()));

        return messages.stream()
                .map(m -> {
                    MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                            .id(m.getId())
                            .fromId(m.getFromUserId())
                            .content(m.getContent())
                            .createdAt(m.getCreatedAt())
                            .isRead(m.getIsRead());

                    // Support both personal and group messages
                    if (m.getType() != null) {
                        builder.type(m.getType().toString())
                                .targetId(m.getTargetId());
                    }

                    // For backward compatibility
                    if (m.getToUserId() != null) {
                        builder.toId(m.getToUserId());
                    } else if (m.getTargetId() != null && m.getType() == Message.MessageType.PERSONAL) {
                        builder.toId(m.getTargetId());
                    }

                    builder.fromUsername(usernamesById.get(m.getFromUserId()));

                    if (m.getToUserId() != null) {
                        builder.toUsername(usernamesById.get(m.getToUserId()));
                    } else if (m.getTargetId() != null && m.getType() == Message.MessageType.PERSONAL) {
                        builder.toUsername(usernamesById.get(m.getTargetId()));
                    }

                    return builder.build();
                })
                .toList();
    }

    /**
     * Gửi tin nhắn nhóm
     * 
     * @param fromUserId User ID của người gửi
     * @param groupId    Group ID của nhóm đích
     * @param content    Nội dung tin nhắn
     * @return MessageResponse - Tin nhắn vừa được gửi
     */
    public MessageResponse pushToGroup(Long fromUserId, Long groupId, String content) {
        try {
            // Validate
            if (fromUserId == null) {
                throw new IllegalArgumentException("fromUserId cannot be null");
            }
            if (groupId == null) {
                throw new IllegalArgumentException("groupId cannot be null");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content cannot be empty");
            }

            // Tạo group message trong database
            Message message = Message.builder()
                    .fromUserId(fromUserId)
                    .targetId(groupId)
                    .content(content)
                    .type(Message.MessageType.GROUP)
                    .build();
            message = messageRepository.save(message);

            // Convert to MessageResponse
            MessageResponse response = MessageResponse.builder()
                    .id(message.getId())
                    .fromId(message.getFromUserId())
                    .targetId(message.getTargetId())
                    .type(message.getType().toString())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .isRead(message.getIsRead())
                    .build();

            log.info("Group message pushed to group {} from user {}: {}", groupId, fromUserId, content);

            // Notify all group members about new message
            notifyGroupNewMessage(groupId, response);

            return response;

        } catch (Exception e) {
            log.error("Error pushing group message to group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to send group message: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử tin nhắn của một nhóm
     * 
     * @param groupId ID của nhóm
     * @return List<MessageResponse> - Danh sách tin nhắn trong nhóm
     */
    public List<MessageResponse> getGroupHistory(Long groupId) {
        try {
            if (groupId == null) {
                throw new IllegalArgumentException("groupId cannot be null");
            }

            List<Message> messages = messageRepository.findByTargetIdAndType(groupId, Message.MessageType.GROUP);
            log.info("Retrieved {} messages from group {}", messages.size(), groupId);

            return convertToResponseList(messages);

        } catch (Exception e) {
            log.error("Error retrieving group history for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to retrieve group history: " + e.getMessage());
        }
    }

    /**
     * Thông báo cho tất cả members trong group có tin nhắn mới
     * (Đây là nơi bạn có thể integrate WebSocket để push real-time)
     */
    private void notifyGroupNewMessage(Long groupId, MessageResponse message) {
        log.info("🔔 GROUP NOTIFICATION: Group {} has new message from {} - '{}'",
                groupId, message.getFromId(), message.getContent());

        try {
            // Gửi qua WebSocket topic cho nhóm
            messagingTemplate.convertAndSend("/topic/groups/" + groupId, message);
            log.info("WS: Group message sent to /topic/groups/{}", groupId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket group notification: {}", e.getMessage());
        }
    }

    /**
     * Create a direct message (from JpaDataApiImpl)
     * 
     * @param fromUserId Sender user ID
     * @param toUserId   Recipient user ID
     * @param content    Message content
     * @return Created message entity
     */
    public Message createMessage(Long fromUserId, Long toUserId, String content) {
        Message message = Message.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .targetId(toUserId) // Fix: Set targetId for PERSONAL messages
                .content(content)
                .type(Message.MessageType.PERSONAL)
                .build();
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByToUserId(Long toUserId) {
        return messageRepository.findByToUserId(toUserId);
    }

    public List<Message> getConversationBetweenUsers(Long userId1, Long userId2) {
        return messageRepository.findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(userId1, userId2, userId1, userId2);
    }

    public void markMessageAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setIsRead(true);
            messageRepository.save(m);
        });
    }

    public void deleteMessageById(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    public Message createGroupMessage(Long fromUserId, Long groupId, String content) {
        Message message = Message.builder()
                .fromUserId(fromUserId)
                .targetId(groupId)
                .content(content)
                .type(Message.MessageType.GROUP)
                .build();
        return messageRepository.save(message);
    }

    public List<Message> getGroupMessageHistory(Long groupId) {
        return messageRepository.findByTargetIdAndType(groupId, Message.MessageType.GROUP);
    }

    public List<Message> getMessagesByTargetIdAndType(Long targetId, Message.MessageType type) {
        return messageRepository.findByTargetIdAndType(targetId, type);
    }
}
