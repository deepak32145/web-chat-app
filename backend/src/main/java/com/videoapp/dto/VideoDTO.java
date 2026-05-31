package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private Long viewCount;
    private UserDTO user;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser;
    private Boolean isDislikedByCurrentUser;
    private LocalDateTime createdAt;
}
