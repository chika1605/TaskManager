package com.chyngyz.taskmanager.dto;

import com.chyngyz.taskmanager.entity.Role;
import lombok.Data;



@Data
public class UserRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role; // если надо менять роль (например, только админ)
}
