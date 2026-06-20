package com.videoapp.controller;

import com.videoapp.dto.CreateMessageRequest;
import com.videoapp.dto.MessageDTO;
import com.videoapp.dto.UserDTO;
import com.videoapp.dto.WebSocketMessageRequest;
import com.videoapp.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class WebSocketControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketController webSocketController;

    private WebSocketMessageRequest wsRequest;
    private MessageDTO messageDTO;

    @BeforeEach
    void setUp() {
        wsRequest = new WebSocketMessageRequest();
        wsRequest.setUserId(1L);
        wsRequest.setConversationId(10L);
        wsRequest.setContent("Hello WS");
        wsRequest.setMediaUrl(null);
        wsRequest.setMediaType(null);

        UserDTO sender = new UserDTO();
        sender.setId(1L);
        sender.setUsername("sender");

        messageDTO = new MessageDTO();
        messageDTO.setId(100L);
        messageDTO.setContent("Hello WS");
        messageDTO.setConversationId(10L);
        messageDTO.setSender(sender);
        messageDTO.setIsRead(false);
        messageDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void handleMessage_savesAndBroadcastsMessage() {
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(messageDTO);

        webSocketController.handleMessage(wsRequest);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/conversation/10"),
                eq(messageDTO)
        );
    }

    @Test
    void handleMessage_mapsRequestFieldsToCreateMessageRequest() {
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(messageDTO);

        webSocketController.handleMessage(wsRequest);

        ArgumentCaptor<CreateMessageRequest> captor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService).sendMessage(eq(1L), captor.capture());

        CreateMessageRequest captured = captor.getValue();
        assertThat(captured.getContent()).isEqualTo("Hello WS");
        assertThat(captured.getConversationId()).isEqualTo(10L);
        assertThat(captured.getMediaUrl()).isNull();
        assertThat(captured.getMediaType()).isNull();
    }

    @Test
    void handleMessage_withMedia_mapsMediaFields() {
        wsRequest.setMediaUrl("http://example.com/photo.jpg");
        wsRequest.setMediaType("image");

        MessageDTO mediaMessage = new MessageDTO();
        mediaMessage.setId(101L);
        mediaMessage.setConversationId(10L);
        mediaMessage.setMediaUrl("http://example.com/photo.jpg");
        mediaMessage.setMediaType("image");
        mediaMessage.setSender(messageDTO.getSender());

        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(mediaMessage);

        webSocketController.handleMessage(wsRequest);

        ArgumentCaptor<CreateMessageRequest> captor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService).sendMessage(eq(1L), captor.capture());
        assertThat(captor.getValue().getMediaUrl()).isEqualTo("http://example.com/photo.jpg");
        assertThat(captor.getValue().getMediaType()).isEqualTo("image");
    }

    @Test
    void handleMessage_serviceReturnsNull_doesNotBroadcast() {
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(null);

        webSocketController.handleMessage(wsRequest);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void handleMessage_broadcastsToCorrectTopic() {
        wsRequest.setConversationId(42L);
        messageDTO.setConversationId(42L);
        when(messageService.sendMessage(eq(1L), any(CreateMessageRequest.class))).thenReturn(messageDTO);

        webSocketController.handleMessage(wsRequest);

        verify(messagingTemplate).convertAndSend(eq("/topic/conversation/42"), any(Object.class));
    }
}
