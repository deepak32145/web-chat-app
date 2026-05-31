package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoRequest {
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
}
