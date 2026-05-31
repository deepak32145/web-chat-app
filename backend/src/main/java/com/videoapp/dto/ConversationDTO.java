package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private String name;
    private Boolean isGroupChat;
    private String groupIcon;
    private UserDTO createdBy;
    private List<UserDTO> participants;
    private List<MessageDTO> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
