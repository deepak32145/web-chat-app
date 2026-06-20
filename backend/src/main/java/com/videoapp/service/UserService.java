package com.videoapp.service;

import com.videoapp.dto.UserDTO;
import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnlineUserTracker onlineUserTracker;
    
    public UserDTO searchUserByPhoneNumber(String phoneNumber) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        
        if (userOptional.isEmpty()) {
            return null;
        }
        
        return convertToDTO(userOptional.get());
    }
    
    public List<UserDTO> getAllOnlineUsers(Long currentUserId) {
        Set<Long> onlineIds = onlineUserTracker.getOnlineUserIds();
        return userRepository.findAllById(onlineIds).stream()
                .filter(u -> !Objects.equals(u.getId(), currentUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }
        
        return convertToDTO(userOptional.get());
    }
    
    public void setUserOnline(Long userId) {
        onlineUserTracker.markOnline(userId);
    }

    public void setUserOffline(Long userId) {
        onlineUserTracker.markOffline(userId);
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
