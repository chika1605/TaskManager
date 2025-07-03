package com.chyngyz.taskmanager.dto;

import lombok.Data;



@Data
public class UserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // например, "ADMIN"
}

