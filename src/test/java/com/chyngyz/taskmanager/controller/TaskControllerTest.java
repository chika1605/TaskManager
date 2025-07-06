package com.chyngyz.taskmanager.controller;

import com.chyngyz.taskmanager.dto.TaskRequest;
import com.chyngyz.taskmanager.dto.TaskResponse;
import com.chyngyz.taskmanager.entity.TaskStatus;
import com.chyngyz.taskmanager.security.JwtUtil;
import com.chyngyz.taskmanager.service.CustomUserDetailsService;
import com.chyngyz.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Description");
        request.setPriority(1);
        request.setStatus(TaskStatus.NEW);
        request.setCategory("Development");

        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Description")
                .priority(1)
                .status(TaskStatus.NEW)
                .category("Development")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Mockito.when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("NEW")); // 👈 исправлено
    }


    @Test
    void getTasks_shouldReturnPage() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .status(TaskStatus.NEW)
                .priority(1)
                .category("Dev")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Mockito.when(taskService.getTasks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Task 1"));
    }

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .status(TaskStatus.NEW)
                .build();

        Mockito.when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteTask_shouldReturnMessage() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Task deleted"));
    }

    @Test
    void updateTaskStatus_shouldReturnUpdatedTask() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Mockito.when(taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS)).thenReturn(task);

        mockMvc.perform(patch("/api/tasks/1/status")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
