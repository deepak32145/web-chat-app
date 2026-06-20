package com.videoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoapp.dto.SearchUserRequest;
import com.videoapp.dto.UserDTO;
import com.videoapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;

    private @NonNull String toJson(Object obj) throws Exception {
        return Objects.requireNonNull(objectMapper.writeValueAsString(obj));
    }

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setPhoneNumber("+911234567890");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setStatus("online");
    }

    @Test
    void searchUser_found_returnsUserDTO() throws Exception {
        when(userService.searchUserByPhoneNumber("+911234567890")).thenReturn(userDTO);

        SearchUserRequest request = new SearchUserRequest();
        request.setPhoneNumber("+911234567890");

        mockMvc.perform(post("/api/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.phoneNumber").value("+911234567890"));
    }

    @Test
    void searchUser_notFound_returnsOkWithNullBody() throws Exception {
        when(userService.searchUserByPhoneNumber("+910000000000")).thenReturn(null);

        SearchUserRequest request = new SearchUserRequest();
        request.setPhoneNumber("+910000000000");

        mockMvc.perform(post("/api/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOnlineUsers_withUserId_returnsOnlineUserList() throws Exception {
        when(userService.getAllOnlineUsers(1L)).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/api/users/online")
                .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("online"));
    }

    @Test
    void getAllOnlineUsers_withoutUserId_usesDefaultMinus1() throws Exception {
        when(userService.getAllOnlineUsers(-1L)).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/api/users/online"))
                .andExpect(status().isOk());

        verify(userService).getAllOnlineUsers(-1L);
    }

    @Test
    void getUserById_found_returnsUserDTO() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void setUserOnline_success_returnsOk() throws Exception {
        doNothing().when(userService).setUserOnline(1L);

        mockMvc.perform(put("/api/users/1/online"))
                .andExpect(status().isOk())
                .andExpect(content().string("User set as online"));
    }

    @Test
    void setUserOffline_put_success_returnsOk() throws Exception {
        doNothing().when(userService).setUserOffline(1L);

        mockMvc.perform(put("/api/users/1/offline"))
                .andExpect(status().isOk())
                .andExpect(content().string("User set as offline"));
    }

    @Test
    void setUserOffline_post_beacon_returnsOk() throws Exception {
        doNothing().when(userService).setUserOffline(1L);

        mockMvc.perform(post("/api/users/1/offline"))
                .andExpect(status().isOk())
                .andExpect(content().string("User set as offline"));
    }

    @Test
    void updateUserProfile_success_returnsOk() throws Exception {
        doNothing().when(userService).updateUserProfile(1L, "Jane", "Smith", "bio text", null);

        mockMvc.perform(put("/api/users/1/profile")
                .param("firstName", "Jane")
                .param("lastName", "Smith")
                .param("bio", "bio text"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));
    }

    @Test
    void updateUserProfile_serviceThrows_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(userService).updateUserProfile(anyLong(), any(), any(), any(), any());

        mockMvc.perform(put("/api/users/1/profile")
                .param("firstName", "Jane"))
                .andExpect(status().isBadRequest());
    }
}
