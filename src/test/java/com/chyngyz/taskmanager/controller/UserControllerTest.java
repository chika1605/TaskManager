package com.chyngyz.taskmanager.controller;

import com.chyngyz.taskmanager.dto.UserRequest;
import com.chyngyz.taskmanager.dto.UserResponse;
import com.chyngyz.taskmanager.entity.Role;
import com.chyngyz.taskmanager.security.JwtUtil;
import com.chyngyz.taskmanager.service.CustomUserDetailsService;
import com.chyngyz.taskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("chyngyz")
                .email("chyngyz@mail.com")
                .firstName("Chyngyz")
                .lastName("Almambetov")
                .role("ADMIN")
                .build();

        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("chyngyz"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("chyngyz")
                .email("chyngyz@mail.com")
                .firstName("Chyngyz")
                .lastName("Almambetov")
                .role("ADMIN")
                .build();

        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("chyngyz"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("chyngyz_updated");
        request.setEmail("updated@mail.com");
        request.setFirstName("Chyngyz");
        request.setLastName("Almambetov");
        request.setPassword("newpass");
        request.setRole(Role.USER);

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("chyngyz_updated")
                .email("updated@mail.com")
                .firstName("Chyngyz")
                .lastName("Almambetov")
                .role("USER")
                .build();

        Mockito.when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("chyngyz_updated"));
    }

    @Test
    void deleteUser_shouldReturnMessage() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));
    }
}
