package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    private String name;
    private Boolean isGroupChat;
    private String groupIcon;
    private List<Long> participantIds;
}
