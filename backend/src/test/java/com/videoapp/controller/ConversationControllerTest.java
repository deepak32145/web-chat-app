package com.videoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoapp.dto.ConversationDTO;
import com.videoapp.dto.CreateConversationRequest;
import com.videoapp.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationService conversationService;

    private ConversationDTO conversationDTO;

    private @NonNull String toJson(Object obj) throws Exception {
        return Objects.requireNonNull(objectMapper.writeValueAsString(obj));
    }

    @BeforeEach
    void setUp() {
        conversationDTO = new ConversationDTO();
        conversationDTO.setId(10L);
        conversationDTO.setName("Test Chat");
        conversationDTO.setIsGroupChat(false);
        conversationDTO.setParticipants(List.of());
        conversationDTO.setMessages(List.of());
        conversationDTO.setCreatedAt(LocalDateTime.now());
        conversationDTO.setUpdatedAt(LocalDateTime.now());
    }

    // --- POST /api/conversations/create ---

    @Test
    void createOrGetConversation_success_returnsConversationDTO() throws Exception {
        when(conversationService.createOrGetConversation(1L, 2L)).thenReturn(conversationDTO);

        mockMvc.perform(post("/api/conversations/create")
                .header("userId", "1")
                .param("otherUserId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Chat"));
    }

    @Test
    void createOrGetConversation_serviceReturnsNull_returnsBadRequest() throws Exception {
        when(conversationService.createOrGetConversation(1L, 99L)).thenReturn(null);

        mockMvc.perform(post("/api/conversations/create")
                .header("userId", "1")
                .param("otherUserId", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to create conversation"));
    }

    @Test
    void createOrGetConversation_serviceThrows_returnsBadRequest() throws Exception {
        when(conversationService.createOrGetConversation(1L, 2L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/conversations/create")
                .header("userId", "1")
                .param("otherUserId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: DB error"));
    }

    // --- POST /api/conversations/group ---

    @Test
    void createGroupConversation_success_returnsGroupDTO() throws Exception {
        ConversationDTO groupDTO = new ConversationDTO();
        groupDTO.setId(20L);
        groupDTO.setName("Team Alpha");
        groupDTO.setIsGroupChat(true);
        groupDTO.setParticipants(List.of());
        groupDTO.setMessages(List.of());

        when(conversationService.createGroupConversation(eq(1L), any(CreateConversationRequest.class)))
                .thenReturn(groupDTO);

        CreateConversationRequest request = new CreateConversationRequest();
        request.setName("Team Alpha");
        request.setIsGroupChat(true);
        request.setParticipantIds(List.of(2L, 3L));

        mockMvc.perform(post("/api/conversations/group")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.isGroupChat").value(true))
                .andExpect(jsonPath("$.name").value("Team Alpha"));
    }

    @Test
    void createGroupConversation_serviceReturnsNull_returnsBadRequest() throws Exception {
        when(conversationService.createGroupConversation(eq(1L), any(CreateConversationRequest.class)))
                .thenReturn(null);

        CreateConversationRequest request = new CreateConversationRequest();
        request.setName("Empty Group");
        request.setParticipantIds(List.of());

        mockMvc.perform(post("/api/conversations/group")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to create group conversation"));
    }

    @Test
    void createGroupConversation_serviceThrows_returnsBadRequest() throws Exception {
        when(conversationService.createGroupConversation(eq(1L), any(CreateConversationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        CreateConversationRequest request = new CreateConversationRequest();
        request.setName("Group");
        request.setParticipantIds(List.of(2L));

        mockMvc.perform(post("/api/conversations/group")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Service error"));
    }

    // --- GET /api/conversations ---

    @Test
    void getUserConversations_returnsListOfConversations() throws Exception {
        when(conversationService.getUserConversations(1L)).thenReturn(List.of(conversationDTO));

        mockMvc.perform(get("/api/conversations")
                .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Test Chat"));
    }

    @Test
    void getUserConversations_noConversations_returnsEmptyList() throws Exception {
        when(conversationService.getUserConversations(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/conversations")
                .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUserConversations_serviceThrows_returnsBadRequest() throws Exception {
        when(conversationService.getUserConversations(1L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/conversations")
                .header("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/conversations/{conversationId} ---

    @Test
    void getConversationDetail_found_returnsConversationDTO() throws Exception {
        when(conversationService.getConversationDetail(10L, 1L)).thenReturn(conversationDTO);

        mockMvc.perform(get("/api/conversations/10")
                .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Chat"));
    }

    @Test
    void getConversationDetail_notFound_returnsBadRequest() throws Exception {
        when(conversationService.getConversationDetail(99L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/conversations/99")
                .header("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Conversation not found"));
    }

    @Test
    void getConversationDetail_serviceThrows_returnsBadRequest() throws Exception {
        when(conversationService.getConversationDetail(10L, 1L))
                .thenThrow(new RuntimeException("Access denied"));

        mockMvc.perform(get("/api/conversations/10")
                .header("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Access denied"));
    }
}
