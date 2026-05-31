package com.videoapp.service;

import com.videoapp.dto.ConversationDTO;
import com.videoapp.dto.CreateConversationRequest;
import com.videoapp.dto.MessageDTO;
import com.videoapp.dto.UserDTO;
import com.videoapp.model.Conversation;
import com.videoapp.model.Message;
import com.videoapp.model.User;
import com.videoapp.repository.ConversationRepository;
import com.videoapp.repository.MessageRepository;
import com.videoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    public ConversationDTO createOrGetConversation(Long userId, Long otherUserId) {
        // For 1-on-1 conversation, check if already exists
        Optional<User> user1 = userRepository.findById(userId);
        Optional<User> user2 = userRepository.findById(otherUserId);
        
        if (user1.isEmpty() || user2.isEmpty()) {
            return null;
        }
        
        // Create conversation name from phone numbers for 1-on-1 chats
        String conversationName = user1.get().getPhoneNumber() + " - " + user2.get().getPhoneNumber();
        
        // Check if conversation already exists
        List<Conversation> conversations = conversationRepository.findConversationsByUser(user1.get());
        for (Conversation conv : conversations) {
            if (!conv.getIsGroupChat() && conv.getParticipants().contains(user2.get())) {
                return convertToDTO(conv);
            }
        }
        
        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setName(conversationName);
        conversation.setIsGroupChat(false);
        conversation.setCreatedBy(user1.get());
        conversation.setParticipants(new ArrayList<>(Arrays.asList(user1.get(), user2.get())));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        Conversation savedConversation = conversationRepository.save(conversation);
        return convertToDTO(savedConversation);
    }
    
    public ConversationDTO createGroupConversation(Long userId, CreateConversationRequest request) {
        Optional<User> creatorOptional = userRepository.findById(userId);
        
        if (creatorOptional.isEmpty()) {
            return null;
        }
        
        List<User> participants = new ArrayList<>();
        participants.add(creatorOptional.get());
        
        for (Long participantId : request.getParticipantIds()) {
            Optional<User> participant = userRepository.findById(participantId);
            if (participant.isPresent()) {
                participants.add(participant.get());
            }
        }
        
        Conversation conversation = new Conversation();
        conversation.setName(request.getName());
        conversation.setIsGroupChat(true);
        conversation.setGroupIcon(request.getGroupIcon());
        conversation.setCreatedBy(creatorOptional.get());
        conversation.setParticipants(participants);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        Conversation savedConversation = conversationRepository.save(conversation);
        return convertToDTO(savedConversation);
    }
    
    public List<ConversationDTO> getUserConversations(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return List.of();
        List<Conversation> conversations = conversationRepository.findConversationsByUser(userOpt.get());
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ConversationDTO getConversationDetail(Long conversationId, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return null;
        Optional<Conversation> conversationOptional = conversationRepository.findConversationByIdAndUser(conversationId, userOpt.get());

        if (conversationOptional.isEmpty()) {
            return null;
        }

        return convertToDTO(conversationOptional.get());
    }
    
    private ConversationDTO convertToDTO(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setName(conversation.getName());
        dto.setIsGroupChat(conversation.getIsGroupChat());
        dto.setGroupIcon(conversation.getGroupIcon());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        
        // Convert participants
        List<UserDTO> participantDTOs = conversation.getParticipants().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
        dto.setParticipants(participantDTOs);
        
        // Get last few messages (latest 50)
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId());
        List<MessageDTO> messageDTOs = messages.stream()
                .limit(50)
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList());
        dto.setMessages(messageDTOs);
        
        return dto;
    }
    
    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setBio(user.getBio());
        dto.setStatus(user.getStatus());
        return dto;
    }
    
    private MessageDTO convertMessageToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setMediaType(message.getMediaType());
        dto.setConversationId(message.getConversation().getId());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setSender(convertUserToDTO(message.getSender()));
        return dto;
    }
}
