package com.chyngyz.taskmanager.integration;

import com.chyngyz.taskmanager.dto.RegisterRequest;
import com.chyngyz.taskmanager.dto.TeamRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TeamIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long userId;

    @BeforeEach
    void setup() throws Exception {
        String unique = String.valueOf(System.currentTimeMillis());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("user_" + unique);
        registerRequest.setEmail("email_" + unique + "@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        token = registerJson.get("token").asText();

        String usersResponse = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        userId = objectMapper.readTree(usersResponse).get(0).get("id").asLong();
    }


    private TeamRequest createRequest() {
        TeamRequest request = new TeamRequest();
        request.setName("Integration Team");
        request.setDescription("Integration testing");
        request.setMemberIds(Set.of(userId));
        return request;
    }

    @Test
    void createTeam_shouldReturnTeam() throws Exception {
        TeamRequest request = createRequest();

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Integration Team"))
                .andExpect(jsonPath("$.createdByUsername").exists());
    }

    @Test
    void getAllTeams_shouldReturnList() throws Exception {
        TeamRequest request = createRequest();

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", not(emptyOrNullString())));
    }

    @Test
    void getTeamById_shouldReturnCorrectTeam() throws Exception {
        TeamRequest request = createRequest();

        String response = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/teams/" + teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId))
                .andExpect(jsonPath("$.name").value("Integration Team"));
    }

    @Test
    void deleteTeam_shouldReturnSuccess() throws Exception {
        // Регистрируем пользователя и получаем токен
        String username = "user_" + System.currentTimeMillis();
        String email = "email_" + System.currentTimeMillis() + "@example.com";

        String registerJson = """
    {
        "username": "%s",
        "email": "%s",
        "password": "password",
        "firstName": "Test",
        "lastName": "User"
    }
    """.formatted(username, email);

        String authResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(authResponse).get("token").asText();

        TeamRequest request = new TeamRequest();
        request.setName("Integration Team");
        request.setDescription("Integration testing");
        request.setMemberIds(Set.of(1L));

        String teamResponse = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(teamResponse).get("id").asLong();

        mockMvc.perform(delete("/api/teams/" + teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
