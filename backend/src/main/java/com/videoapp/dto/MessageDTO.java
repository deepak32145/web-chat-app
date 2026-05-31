package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private UserDTO sender;
    private Long conversationId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
