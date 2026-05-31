package com.videoapp.service;

import com.videoapp.dto.UserDTO;
import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserDTO searchUserByPhoneNumber(String phoneNumber) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        
        if (userOptional.isEmpty()) {
            return null;
        }
        
        return convertToDTO(userOptional.get());
    }
    
    public List<UserDTO> getAllOnlineUsers(Long currentUserId) {
        List<User> onlineUsers = userRepository.findAllOnlineUsersExcluding(currentUserId);
        return onlineUsers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }
        
        return convertToDTO(userOptional.get());
    }
    
    public void setUserOnline(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus("online");
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    public void setUserOffline(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus("offline");
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    public void updateUserProfile(Long userId, String firstName, String lastName, String bio, String profilePicture) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);
            if (bio != null) user.setBio(bio);
            if (profilePicture != null) user.setProfilePicture(profilePicture);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setBio(user.getBio());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
