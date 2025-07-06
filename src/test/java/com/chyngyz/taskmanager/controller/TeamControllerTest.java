package com.chyngyz.taskmanager.controller;

import com.chyngyz.taskmanager.dto.TeamRequest;
import com.chyngyz.taskmanager.dto.TeamResponse;
import com.chyngyz.taskmanager.security.JwtUtil;
import com.chyngyz.taskmanager.service.CustomUserDetailsService;
import com.chyngyz.taskmanager.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTeam_shouldReturnTeam() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setName("Backend Team");
        request.setDescription("Handles all backend logic");
        request.setMemberIds(Set.of(1L, 2L));

        TeamResponse response = TeamResponse.builder()
                .id(1L)
                .name("Backend Team")
                .description("Handles all backend logic")
                .createdByUsername("admin")
                .memberUsernames(Set.of("user1", "user2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(teamService.createTeam(any(TeamRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Backend Team"))
                .andExpect(jsonPath("$.createdByUsername").value("admin"))
                .andExpect(jsonPath("$.memberUsernames").isArray());
    }

    @Test
    void getAllTeams_shouldReturnList() throws Exception {
        TeamResponse team = TeamResponse.builder()
                .id(1L)
                .name("DevOps")
                .description("Infra stuff")
                .createdByUsername("manager")
                .memberUsernames(Set.of("user1"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(teamService.getAllTeams()).thenReturn(List.of(team));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("DevOps"))
                .andExpect(jsonPath("$[0].createdByUsername").value("manager"));
    }

    @Test
    void getTeamById_shouldReturnTeam() throws Exception {
        TeamResponse team = TeamResponse.builder()
                .id(1L)
                .name("QA")
                .description("Testing")
                .createdByUsername("qa_lead")
                .memberUsernames(Set.of("qa1"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(teamService.getTeamById(1L)).thenReturn(team);

        mockMvc.perform(get("/api/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("QA"))
                .andExpect(jsonPath("$.createdByUsername").value("qa_lead"));
    }

    @Test
    void deleteTeam_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/teams/1"))
                .andExpect(status().isOk());
    }
}
