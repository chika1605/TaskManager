package com.chyngyz.taskmanager.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
