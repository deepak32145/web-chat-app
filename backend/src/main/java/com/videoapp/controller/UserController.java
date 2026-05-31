package com.videoapp.controller;

import com.videoapp.dto.SearchUserRequest;
import com.videoapp.dto.UserDTO;
import com.videoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/search")
    public ResponseEntity<?> searchUser(@RequestBody SearchUserRequest request) {
        try {
            UserDTO userDTO = userService.searchUserByPhoneNumber(request.getPhoneNumber());
            if (userDTO == null) {
                return ResponseEntity.ok(null); // User not found
            }
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/online")
    public ResponseEntity<?> getAllOnlineUsers(@RequestHeader(value = "userId", required = false) Long userId) {
        try {
            List<UserDTO> onlineUsers = userService.getAllOnlineUsers(userId != null ? userId : -1L);
            return ResponseEntity.ok(onlineUsers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserDTO userDTO = userService.getUserById(userId);
            if (userDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{userId}/online")
    public ResponseEntity<?> setUserOnline(@PathVariable Long userId) {
        try {
            userService.setUserOnline(userId);
            return ResponseEntity.ok("User set as online");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{userId}/offline")
    public ResponseEntity<?> setUserOffline(@PathVariable Long userId) {
        try {
            userService.setUserOffline(userId);
            return ResponseEntity.ok("User set as offline");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String profilePicture) {
        try {
            userService.updateUserProfile(userId, firstName, lastName, bio, profilePicture);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
