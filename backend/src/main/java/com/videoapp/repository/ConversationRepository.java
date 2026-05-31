package com.videoapp.repository;

import com.videoapp.model.Conversation;
import com.videoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p = :user ORDER BY c.updatedAt DESC")
    List<Conversation> findConversationsByUser(@Param("user") User user);

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE c.id = :conversationId AND p = :user")
    Optional<Conversation> findConversationByIdAndUser(@Param("conversationId") Long conversationId, @Param("user") User user);
}
