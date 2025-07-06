package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.TaskRequest;
import com.chyngyz.taskmanager.dto.TaskResponse;
import com.chyngyz.taskmanager.entity.*;
import com.chyngyz.taskmanager.repository.TaskRepository;
import com.chyngyz.taskmanager.repository.TeamRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_shouldReturnTaskResponse() {
        String username = "creator";
        User creator = User.builder().id(1L).username(username).build();
        TaskRequest request = new TaskRequest();
        request.setTitle("Test");
        request.setStatus(TaskStatus.NEW);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(creator));
        mockSecurityContext(username);

        Task savedTask = Task.builder().id(1L).title("Test").createdBy(creator).status(TaskStatus.NEW).build();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        var response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Test", response.getTitle());
    }

    @Test
    void updateTask_shouldUpdateFields() {
        Task existingTask = Task.builder().id(1L).title("Old").status(TaskStatus.NEW).build();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated");
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any())).thenReturn(existingTask);

        var response = taskService.updateTask(1L, request);

        assertEquals("Updated", response.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void deleteTask_shouldCallRepository() {
        taskService.deleteTask(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void getTaskById_shouldReturnTaskResponse() {
        Task task = Task.builder().id(1L).title("Task").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        var response = taskService.getTaskById(1L);
        assertEquals("Task", response.getTitle());
    }

    @Test
    void updateTaskStatus_shouldUpdateAndReturn() {
        Task task = Task.builder().id(1L).status(TaskStatus.NEW).build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        var response = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
    }

    @Test
    void assignTask_shouldAssignUser() {
        Task task = Task.builder().id(1L).build();
        User user = User.builder().id(2L).build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);

        var response = taskService.assignTask(1L, 2L);
        assertEquals(2L, response.getAssignedToId());
    }

    @Test
    void getTasks_shouldReturnFilteredPage() {
        Task task = Task.builder().id(1L).title("Task").status(TaskStatus.NEW).build();
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findByStatus(eq(TaskStatus.NEW), any(Pageable.class))).thenReturn(taskPage);

        Page<TaskResponse> result = taskService.getTasks(null, TaskStatus.NEW, null, null, null, null, 0, 10, "createdAt");

        assertEquals(1, result.getTotalElements());
        assertEquals("Task", result.getContent().get(0).getTitle());
    }

    private void mockSecurityContext(String username) {
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn(username);
        var ctx = mock(org.springframework.security.core.context.SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(ctx);
    }
}
