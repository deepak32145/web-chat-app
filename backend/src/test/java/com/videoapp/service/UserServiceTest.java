package com.videoapp.service;

import com.videoapp.dto.UserDTO;
import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OnlineUserTracker onlineUserTracker;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPhoneNumber("+911234567890");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setBio("Hello");
        user.setStatus("online");
        user.setProfilePicture("pic.jpg");
    }

    @Test
    void searchUserByPhoneNumber_found_returnsDTO() {
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(user));

        UserDTO result = userService.searchUserByPhoneNumber("+911234567890");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhoneNumber()).isEqualTo("+911234567890");
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void searchUserByPhoneNumber_notFound_returnsNull() {
        when(userRepository.findByPhoneNumber("+910000000000")).thenReturn(Optional.empty());

        UserDTO result = userService.searchUserByPhoneNumber("+910000000000");

        assertThat(result).isNull();
    }

    @Test
    void getAllOnlineUsers_returnsListExcludingCurrentUser() {
        User other = new User();
        other.setId(2L);
        other.setUsername("other");
        other.setPhoneNumber("+919999999999");
        other.setStatus("online");

        // Both user (id=1) and other (id=2) are tracked as online
        when(onlineUserTracker.getOnlineUserIds()).thenReturn(Set.of(1L, 2L));
        when(userRepository.findAllById(anyIterable())).thenReturn(List.of(user, other));

        List<UserDTO> result = userService.getAllOnlineUsers(1L);

        // Current user (id=1) must be filtered out
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void getAllOnlineUsers_noOnlineUsers_returnsEmptyList() {
        when(onlineUserTracker.getOnlineUserIds()).thenReturn(Set.of());
        when(userRepository.findAllById(anyIterable())).thenReturn(List.of());

        List<UserDTO> result = userService.getAllOnlineUsers(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_found_returnsDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_notFound_returnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserDTO result = userService.getUserById(99L);

        assertThat(result).isNull();
    }

    @Test
    void setUserOnline_callsMarkOnline() {
        userService.setUserOnline(1L);

        verify(onlineUserTracker).markOnline(1L);
        verifyNoInteractions(userRepository);
    }

    @Test
    void setUserOffline_callsMarkOffline() {
        userService.setUserOffline(1L);

        verify(onlineUserTracker).markOffline(1L);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserProfile_updatesProvidedFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUserProfile(1L, "Jane", "Smith", "New bio", "new_pic.jpg");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("Jane");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.getBio()).isEqualTo("New bio");
        assertThat(saved.getProfilePicture()).isEqualTo("new_pic.jpg");
    }

    @Test
    void updateUserProfile_nullFields_keepsOriginalValues() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUserProfile(1L, null, null, null, null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
    }

    @Test
    void updateUserProfile_userNotFound_doesNothing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userService.updateUserProfile(99L, "Jane", null, null, null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void convertToDTO_mapsAllFields() {
        UserDTO dto = userService.convertToDTO(user);

        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getUsername()).isEqualTo(user.getUsername());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getPhoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(dto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(dto.getLastName()).isEqualTo(user.getLastName());
        assertThat(dto.getProfilePicture()).isEqualTo(user.getProfilePicture());
        assertThat(dto.getBio()).isEqualTo(user.getBio());
        assertThat(dto.getStatus()).isEqualTo(user.getStatus());
    }
}
