package com.hdtpt.pentachat.message.service;

import com.hdtpt.pentachat.message.dto.response.MessageResponse;
import com.hdtpt.pentachat.message.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Message Service Tests")
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    @DisplayName("Should send personal message successfully")
    void testPushToUser() {
        MessageResponse response = messageService.pushToUser(1L, 2L, "Hello Bob!");

        assertNotNull(response);
        assertEquals("Hello Bob!", response.getContent());
        assertEquals(1L, response.getFromId());
        assertEquals(2L, response.getToId());
    }

    @Test
    @DisplayName("Should get user inbox")
    void testGetUserInbox() {
        messageService.pushToUser(1L, 2L, "Message 1");
        messageService.pushToUser(3L, 2L, "Message 2");

        List<MessageResponse> inbox = messageService.getUserInbox(2L);
        assertEquals(2, inbox.size());
    }

    @Test
    @DisplayName("Should send group message successfully")
    void testPushToGroup() {
        MessageResponse response = messageService.pushToGroup(1L, 10L, "Hello Group!");

        assertNotNull(response);
        assertEquals("Hello Group!", response.getContent());
        assertEquals(1L, response.getFromId());
        assertEquals(10L, response.getTargetId());
        assertEquals("GROUP", response.getType());
    }

    @Test
    @DisplayName("Should get group message history")
    void testGetGroupHistory() {
        messageService.pushToGroup(1L, 10L, "Group Msg 1");
        messageService.pushToGroup(2L, 10L, "Group Msg 2");

        List<MessageResponse> history = messageService.getGroupHistory(10L);
        assertEquals(2, history.size());
    }

    @Test
    @DisplayName("Should reject markAsRead when user is not recipient")
    void testMarkAsRead_UnauthorizedUser() {
        MessageResponse response = messageService.pushToUser(1L, 2L, "Private message");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.markAsRead(3L, response.getId()));

        assertEquals("User is not allowed to mark this message as read", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject deleteMessage when user is unrelated")
    void testDeleteMessage_UnauthorizedUser() {
        MessageResponse response = messageService.pushToUser(1L, 2L, "Do not delete");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.deleteMessage(3L, response.getId()));

        assertEquals("User is not allowed to delete this message", exception.getMessage());
    }

    @Test
    @DisplayName("Should allow sender to delete message")
    void testDeleteMessage_SenderCanDelete() {
        MessageResponse response = messageService.pushToUser(1L, 2L, "Delete by sender");

        messageService.deleteMessage(1L, response.getId());

        assertTrue(messageRepository.findById(response.getId()).isEmpty());
    }
}
