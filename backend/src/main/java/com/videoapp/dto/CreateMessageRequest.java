package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequest {
    private String content;
    private String mediaUrl;
    private String mediaType;
    private Long conversationId;
}
