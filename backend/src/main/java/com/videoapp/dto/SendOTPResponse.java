package com.videoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOTPResponse {
    private String message;
    private Boolean success;
    private Boolean isNewUser;
}
