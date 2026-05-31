package com.videoapp.repository;

import com.videoapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = ?1 AND m.isRead = false")
    List<Message> findUnreadMessagesInConversation(Long conversationId);
}
