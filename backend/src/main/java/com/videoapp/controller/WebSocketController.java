package com.videoapp.controller;

import com.videoapp.dto.CreateMessageRequest;
import com.videoapp.dto.MessageDTO;
import com.videoapp.dto.WebSocketMessageRequest;
import com.videoapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void handleMessage(@Payload WebSocketMessageRequest request) {
        CreateMessageRequest createRequest = new CreateMessageRequest();
        createRequest.setContent(request.getContent());
        createRequest.setMediaUrl(request.getMediaUrl());
        createRequest.setMediaType(request.getMediaType());
        createRequest.setConversationId(request.getConversationId());

        MessageDTO messageDTO = messageService.sendMessage(request.getUserId(), createRequest);

        if (messageDTO != null) {
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId(),
                messageDTO
            );
        }
    }
}
