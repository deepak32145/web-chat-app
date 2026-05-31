package com.videoapp.controller;

import com.videoapp.dto.ConversationDTO;
import com.videoapp.dto.CreateConversationRequest;
import com.videoapp.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {
    
    @Autowired
    private ConversationService conversationService;
    
    @PostMapping("/create")
    public ResponseEntity<?> createOrGetConversation(
            @RequestHeader("userId") Long userId,
            @RequestParam Long otherUserId) {
        try {
            ConversationDTO conversationDTO = conversationService.createOrGetConversation(userId, otherUserId);
            if (conversationDTO == null) {
                return ResponseEntity.badRequest().body("Failed to create conversation");
            }
            return ResponseEntity.ok(conversationDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/group")
    public ResponseEntity<?> createGroupConversation(
            @RequestHeader("userId") Long userId,
            @RequestBody CreateConversationRequest request) {
        try {
            ConversationDTO conversationDTO = conversationService.createGroupConversation(userId, request);
            if (conversationDTO == null) {
                return ResponseEntity.badRequest().body("Failed to create group conversation");
            }
            return ResponseEntity.ok(conversationDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getUserConversations(@RequestHeader("userId") Long userId) {
        try {
            List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getConversationDetail(
            @PathVariable Long conversationId,
            @RequestHeader("userId") Long userId) {
        try {
            ConversationDTO conversationDTO = conversationService.getConversationDetail(conversationId, userId);
            if (conversationDTO == null) {
                return ResponseEntity.badRequest().body("Conversation not found");
            }
            return ResponseEntity.ok(conversationDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
