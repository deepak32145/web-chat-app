package com.videoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoapp.dto.CreateMessageRequest;
import com.videoapp.dto.MessageDTO;
import com.videoapp.dto.UserDTO;
import com.videoapp.service.MessageService;
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

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private MessageDTO messageDTO;

    private @NonNull String toJson(Object obj) throws Exception {
        return Objects.requireNonNull(objectMapper.writeValueAsString(obj));
    }

    @BeforeEach
    void setUp() {
        UserDTO sender = new UserDTO();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setPhoneNumber("+911234567890");

        messageDTO = new MessageDTO();
        messageDTO.setId(100L);
        messageDTO.setContent("Hello");
        messageDTO.setConversationId(10L);
        messageDTO.setIsRead(false);
        messageDTO.setSender(sender);
        messageDTO.setCreatedAt(LocalDateTime.now());
    }

    // --- POST /api/messages ---

    @Test
    void sendMessage_success_returnsMessageDTO() throws Exception {
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(messageDTO);

        CreateMessageRequest request = new CreateMessageRequest();
        request.setContent("Hello");
        request.setConversationId(10L);

        mockMvc.perform(post("/api/messages")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.conversationId").value(10));
    }

    @Test
    void sendMessage_serviceReturnsNull_returnsBadRequest() throws Exception {
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(null);

        CreateMessageRequest request = new CreateMessageRequest();
        request.setContent("Hello");
        request.setConversationId(10L);

        mockMvc.perform(post("/api/messages")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to send message"));
    }

    @Test
    void sendMessage_serviceThrows_returnsBadRequest() throws Exception {
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class)))
                .thenThrow(new RuntimeException("DB error"));

        CreateMessageRequest request = new CreateMessageRequest();
        request.setContent("Hello");
        request.setConversationId(10L);

        mockMvc.perform(post("/api/messages")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: DB error"));
    }

    @Test
    void sendMessage_withMedia_returnsMessageDTOWithMediaFields() throws Exception {
        messageDTO.setMediaUrl("http://example.com/photo.jpg");
        messageDTO.setMediaType("image");
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(messageDTO);

        CreateMessageRequest request = new CreateMessageRequest();
        request.setContent("");
        request.setConversationId(10L);
        request.setMediaUrl("http://example.com/photo.jpg");
        request.setMediaType("image");

        mockMvc.perform(post("/api/messages")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaUrl").value("http://example.com/photo.jpg"))
                .andExpect(jsonPath("$.mediaType").value("image"));
    }

    // --- GET /api/messages/conversation/{conversationId} ---

    @Test
    void getMessages_returnsListOfMessages() throws Exception {
        when(messageService.getMessagesByConversation(10L)).thenReturn(List.of(messageDTO));

        mockMvc.perform(get("/api/messages/conversation/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    void getMessages_noMessages_returnsEmptyList() throws Exception {
        when(messageService.getMessagesByConversation(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/conversation/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMessages_serviceThrows_returnsBadRequest() throws Exception {
        when(messageService.getMessagesByConversation(10L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/messages/conversation/10"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/messages/unread/{conversationId} ---

    @Test
    void getUnreadMessages_returnsUnreadList() throws Exception {
        when(messageService.getUnreadMessages(10L)).thenReturn(List.of(messageDTO));

        mockMvc.perform(get("/api/messages/unread/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void getUnreadMessages_none_returnsEmptyList() throws Exception {
        when(messageService.getUnreadMessages(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/unread/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUnreadMessages_serviceThrows_returnsBadRequest() throws Exception {
        when(messageService.getUnreadMessages(10L))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/messages/unread/10"))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/messages/{messageId}/read ---

    @Test
    void markMessageAsRead_success_returnsOk() throws Exception {
        doNothing().when(messageService).markMessageAsRead(100L);

        mockMvc.perform(put("/api/messages/100/read"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message marked as read"));
    }

    @Test
    void markMessageAsRead_serviceThrows_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Not found")).when(messageService).markMessageAsRead(999L);

        mockMvc.perform(put("/api/messages/999/read"))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/messages/conversation/{conversationId}/read ---

    @Test
    void markConversationAsRead_success_returnsOk() throws Exception {
        doNothing().when(messageService).markConversationAsRead(10L);

        mockMvc.perform(put("/api/messages/conversation/10/read"))
                .andExpect(status().isOk())
                .andExpect(content().string("Conversation marked as read"));
    }

    @Test
    void markConversationAsRead_serviceThrows_returnsBadRequest() throws Exception {
        doThrow(new RuntimeException("Error")).when(messageService).markConversationAsRead(10L);

        mockMvc.perform(put("/api/messages/conversation/10/read"))
                .andExpect(status().isBadRequest());
    }
}
