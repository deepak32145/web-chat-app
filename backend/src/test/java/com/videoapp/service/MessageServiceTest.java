package com.videoapp.service;

import com.videoapp.dto.CreateMessageRequest;
import com.videoapp.dto.MessageDTO;
import com.videoapp.model.Conversation;
import com.videoapp.model.Message;
import com.videoapp.model.User;
import com.videoapp.repository.ConversationRepository;
import com.videoapp.repository.MessageRepository;
import com.videoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private Conversation conversation;
    private Message message;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setPhoneNumber("+911234567890");

        conversation = new Conversation();
        conversation.setId(10L);
        conversation.setName("Test Conv");
        conversation.setIsGroupChat(false);
        conversation.setCreatedBy(sender);
        conversation.setParticipants(new ArrayList<>());

        message = new Message();
        message.setId(100L);
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent("Hello");
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void sendMessage_validRequest_returnsMessageDTO() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setConversationId(10L);
        request.setContent("Hello");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        MessageDTO result = messageService.sendMessage(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello");
        assertThat(result.getSender().getId()).isEqualTo(1L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_userNotFound_returnsNull() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setConversationId(10L);
        request.setContent("Hello");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));

        MessageDTO result = messageService.sendMessage(99L, request);

        assertThat(result).isNull();
        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_conversationNotFound_returnsNull() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setConversationId(99L);
        request.setContent("Hello");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        MessageDTO result = messageService.sendMessage(1L, request);

        assertThat(result).isNull();
        verify(messageRepository, never()).save(any());
    }

    @Test
    void getMessagesByConversation_returnsMappedDTOs() {
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(message));

        List<MessageDTO> result = messageService.getMessagesByConversation(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void getMessagesByConversation_noMessages_returnsEmptyList() {
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        List<MessageDTO> result = messageService.getMessagesByConversation(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void getUnreadMessages_returnsUnreadDTOs() {
        when(messageRepository.findUnreadMessagesInConversation(10L)).thenReturn(List.of(message));

        List<MessageDTO> result = messageService.getUnreadMessages(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsRead()).isFalse();
    }

    @Test
    void markMessageAsRead_existingMessage_setsIsReadTrue() {
        when(messageRepository.findById(100L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        messageService.markMessageAsRead(100L);

        verify(messageRepository).save(argThat(m -> m.getIsRead()));
    }

    @Test
    void markMessageAsRead_notFound_doesNothing() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        messageService.markMessageAsRead(999L);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void markConversationAsRead_marksAllUnreadMessages() {
        Message unread1 = new Message();
        unread1.setId(101L);
        unread1.setSender(sender);
        unread1.setConversation(conversation);
        unread1.setContent("msg1");
        unread1.setIsRead(false);

        Message unread2 = new Message();
        unread2.setId(102L);
        unread2.setSender(sender);
        unread2.setConversation(conversation);
        unread2.setContent("msg2");
        unread2.setIsRead(false);

        when(messageRepository.findUnreadMessagesInConversation(10L)).thenReturn(List.of(unread1, unread2));

        messageService.markConversationAsRead(10L);

        verify(messageRepository).saveAll(argThat(msgs -> {
            List<Message> list = new ArrayList<>();
            msgs.forEach(list::add);
            return list.stream().allMatch(Message::getIsRead);
        }));
    }
}
