package com.chyngyz.taskmanager.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
