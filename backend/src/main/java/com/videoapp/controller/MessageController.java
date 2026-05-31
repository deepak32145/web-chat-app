package com.videoapp.controller;

import com.videoapp.dto.CreateMessageRequest;
import com.videoapp.dto.MessageDTO;
import com.videoapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestHeader("userId") Long userId,
            @RequestBody CreateMessageRequest request) {
        try {
            MessageDTO messageDTO = messageService.sendMessage(userId, request);
            if (messageDTO == null) {
                return ResponseEntity.badRequest().body("Failed to send message");
            }
            return ResponseEntity.ok(messageDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getMessages(@PathVariable Long conversationId) {
        try {
            List<MessageDTO> messages = messageService.getMessagesByConversation(conversationId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/unread/{conversationId}")
    public ResponseEntity<?> getUnreadMessages(@PathVariable Long conversationId) {
        try {
            List<MessageDTO> messages = messageService.getUnreadMessages(conversationId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {
        try {
            messageService.markMessageAsRead(messageId);
            return ResponseEntity.ok("Message marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/conversation/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(@PathVariable Long conversationId) {
        try {
            messageService.markConversationAsRead(conversationId);
            return ResponseEntity.ok("Conversation marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
