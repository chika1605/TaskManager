package com.chyngyz.taskmanager.integration;

import com.chyngyz.taskmanager.dto.RegisterRequest;
import com.chyngyz.taskmanager.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chyngyz.taskmanager.entity.Role;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest getRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        return request;
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getRegisterRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).get("token").asText();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].username", not(emptyOrNullString())));
    }


    @Test
    void getUserById_shouldReturnCorrectUser() throws Exception {
        // 1. Регистрация
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getRegisterRequest())))
                .andReturn().getResponse().getContentAsString();

        // 2. Достаем токен
        String token = objectMapper.readTree(response).get("token").asText();

        // 3. Получаем список пользователей с токеном
        String usersJson = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 4. Ищем ID пользователя
        Long userId = null;
        for (var node : objectMapper.readTree(usersJson)) {
            if (node.get("username").asText().equals("testuser")) {
                userId = node.get("id").asLong();
                break;
            }
        }

        assert userId != null;

        // 5. Получаем пользователя по id
        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testuser"));
    }



    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        // Шаг 1: регистрация пользователя
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getRegisterRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).get("token").asText();

        // Шаг 2: получить список пользователей с токеном
        String usersJson = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(usersJson).get(0).get("id").asLong();

        // Шаг 3: отправка запроса на обновление пользователя с токеном
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");
        updateRequest.setPassword("newpassword");
        updateRequest.setRole(Role.USER);

        mockMvc.perform(put("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }


    @Test
    void deleteUser_shouldReturnConfirmation() throws Exception {
        // 1. Регистрируем пользователя
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getRegisterRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).get("token").asText();

        // 2. Получаем всех пользователей, чтобы взять id
        String usersResponse = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(usersResponse).get(0).get("id").asLong();

        // 3. Удаляем пользователя
        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));
    }
}
