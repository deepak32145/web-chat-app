package com.videoapp.service;

import com.videoapp.dto.CreateMessageRequest;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public MessageDTO sendMessage(Long userId, CreateMessageRequest request) {
        Optional<User> senderOptional = userRepository.findById(userId);
        Optional<Conversation> conversationOptional = conversationRepository.findById(request.getConversationId());
        
        if (senderOptional.isEmpty() || conversationOptional.isEmpty()) {
            return null;
        }
        
        Message message = new Message();
        message.setSender(senderOptional.get());
        message.setConversation(conversationOptional.get());
        message.setContent(request.getContent());
        message.setMediaUrl(request.getMediaUrl());
        message.setMediaType(request.getMediaType());
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation's updated_at
        Conversation conversation = conversationOptional.get();
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return convertToDTO(savedMessage);
    }
    
    public List<MessageDTO> getMessagesByConversation(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public List<MessageDTO> getUnreadMessages(Long conversationId) {
        List<Message> messages = messageRepository.findUnreadMessagesInConversation(conversationId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public void markMessageAsRead(Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }
    
    public void markConversationAsRead(Long conversationId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesInConversation(conversationId);
        unreadMessages.forEach(msg -> msg.setIsRead(true));
        messageRepository.saveAll(unreadMessages);
    }
    
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setMediaType(message.getMediaType());
        dto.setConversationId(message.getConversation().getId());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        
        UserDTO senderDTO = new UserDTO();
        senderDTO.setId(message.getSender().getId());
        senderDTO.setUsername(message.getSender().getUsername());
        senderDTO.setPhoneNumber(message.getSender().getPhoneNumber());
        senderDTO.setProfilePicture(message.getSender().getProfilePicture());
        dto.setSender(senderDTO);
        
        return dto;
    }
}
