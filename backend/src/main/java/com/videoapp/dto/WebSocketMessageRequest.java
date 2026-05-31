package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageRequest {
    private Long userId;
    private Long conversationId;
    private String content;
    private String mediaUrl;
    private String mediaType;
}
