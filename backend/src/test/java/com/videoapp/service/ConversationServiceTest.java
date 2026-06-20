package com.videoapp.service;

import com.videoapp.dto.ConversationDTO;
import com.videoapp.dto.CreateConversationRequest;
import com.videoapp.model.Conversation;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private UserRepository userRepository;
    @Mock private MessageRepository messageRepository;

    @InjectMocks
    private ConversationService conversationService;

    private User user1;
    private User user2;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setPhoneNumber("+911111111111");
        user1.setFirstName("Alice");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setPhoneNumber("+922222222222");
        user2.setFirstName("Bob");

        conversation = new Conversation();
        conversation.setId(10L);
        conversation.setName("+911111111111 - +922222222222");
        conversation.setIsGroupChat(false);
        conversation.setCreatedBy(user1);
        conversation.setParticipants(new ArrayList<>(Arrays.asList(user1, user2)));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createOrGetConversation_existingConversation_returnsExisting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(conversationRepository.findConversationsByUser(user1)).thenReturn(List.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        ConversationDTO result = conversationService.createOrGetConversation(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void createOrGetConversation_newConversation_createsAndReturns() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(conversationRepository.findConversationsByUser(user1)).thenReturn(List.of());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        ConversationDTO result = conversationService.createOrGetConversation(1L, 2L);

        assertThat(result).isNotNull();
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void createOrGetConversation_user1NotFound_returnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        ConversationDTO result = conversationService.createOrGetConversation(99L, 2L);

        assertThat(result).isNull();
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void createOrGetConversation_user2NotFound_returnsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ConversationDTO result = conversationService.createOrGetConversation(1L, 99L);

        assertThat(result).isNull();
    }

    @Test
    void createGroupConversation_success_returnsGroupDTO() {
        Conversation groupConv = new Conversation();
        groupConv.setId(20L);
        groupConv.setName("Team Chat");
        groupConv.setIsGroupChat(true);
        groupConv.setCreatedBy(user1);
        groupConv.setParticipants(new ArrayList<>(Arrays.asList(user1, user2)));
        groupConv.setCreatedAt(LocalDateTime.now());
        groupConv.setUpdatedAt(LocalDateTime.now());

        CreateConversationRequest request = new CreateConversationRequest();
        request.setName("Team Chat");
        request.setIsGroupChat(true);
        request.setParticipantIds(List.of(2L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(groupConv);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(20L)).thenReturn(List.of());

        ConversationDTO result = conversationService.createGroupConversation(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getIsGroupChat()).isTrue();
        assertThat(result.getName()).isEqualTo("Team Chat");
    }

    @Test
    void createGroupConversation_creatorNotFound_returnsNull() {
        CreateConversationRequest request = new CreateConversationRequest();
        request.setName("Team Chat");
        request.setParticipantIds(List.of(2L));

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ConversationDTO result = conversationService.createGroupConversation(99L, request);

        assertThat(result).isNull();
    }

    @Test
    void getUserConversations_returnsUserConversations() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(conversationRepository.findConversationsByUser(user1)).thenReturn(List.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        List<ConversationDTO> result = conversationService.getUserConversations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    void getUserConversations_userNotFound_returnsEmptyList() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        List<ConversationDTO> result = conversationService.getUserConversations(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getConversationDetail_found_returnsDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(conversationRepository.findConversationByIdAndUser(10L, user1)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        ConversationDTO result = conversationService.getConversationDetail(10L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getConversationDetail_notFound_returnsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(conversationRepository.findConversationByIdAndUser(99L, user1)).thenReturn(Optional.empty());

        ConversationDTO result = conversationService.getConversationDetail(99L, 1L);

        assertThat(result).isNull();
    }
}
