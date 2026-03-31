package com.hdtpt.pentachat.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.message.model.Message.MessageType;

import java.util.List;

/**
 * JPA Repository for Message entity
 * Provides database access methods for both personal and group messages
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages sent to a specific user (backward compatibility)
     */
    List<Message> findByToUserId(Long toUserId);

    /**
     * Find all messages sent from a specific user
     */
    List<Message> findByFromUserId(Long fromUserId);

    /**
     * Find all messages between two users (conversation)
     */
    List<Message> findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(
            Long fromUserId1, Long toUserId1,
            Long toUserId2, Long fromUserId2);

    /**
     * Find all messages for a specific target (user or group) with type
     */
    @Query("SELECT m FROM Message m WHERE m.targetId = :targetId AND m.type = :type ORDER BY m.createdAt ASC")
    List<Message> findByTargetIdAndType(@Param("targetId") Long targetId, @Param("type") MessageType type);

    /**
     * Find all group messages for a specific group ID
     */
    @Query("SELECT m FROM Message m WHERE m.targetId = :groupId AND m.type = 'GROUP' ORDER BY m.createdAt ASC")
    List<Message> findGroupHistory(@Param("groupId") Long groupId);

    /**
     * Find all personal messages for a specific user
     */
    @Query("SELECT m FROM Message m WHERE m.targetId = :userId AND m.type = 'PERSONAL' ORDER BY m.createdAt ASC")
    List<Message> findPersonalMessages(@Param("userId") Long userId);
}
