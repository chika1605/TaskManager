package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.UserRequest;
import com.chyngyz.taskmanager.dto.UserResponse;
import com.chyngyz.taskmanager.entity.*;
import com.chyngyz.taskmanager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_shouldReturnList() {
        User user = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@test.com")
                .firstName("First")
                .lastName("Last")
                .role(Role.USER)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    void getUserById_shouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@test.com")
                .firstName("First")
                .lastName("Last")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertEquals("user1", result.getUsername());
        assertEquals("user1@test.com", result.getEmail());
    }

    @Test
    void getUserById_shouldThrowIfNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void updateUser_shouldUpdateFields() {
        User user = User.builder()
                .id(1L)
                .username("olduser")
                .email("old@test.com")
                .firstName("Old")
                .lastName("User")
                .role(Role.USER)
                .build();

        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setFirstName("New");
        request.setLastName("Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("newuser", response.getUsername());
        assertEquals("new@test.com", response.getEmail());
    }

    @Test
    void deleteUser_shouldUnassignTasksAndRemoveUser() {
        User user = User.builder().id(1L).username("deleteMe").build();

        Task task = Task.builder().id(1L).assignedTo(user).build();
        Team team = Team.builder().id(1L).members(new HashSet<>(Set.of(user))).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findAllByAssignedTo(user)).thenReturn(List.of(task));
        when(teamRepository.findAllByMembersContains(user)).thenReturn(List.of(team));

        userService.deleteUser(1L);

        assertNull(task.getAssignedTo());
        assertFalse(team.getMembers().contains(user));

        verify(taskRepository).saveAll(any());
        verify(teamRepository).saveAll(any());
        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(userRepository).delete(user);
    }
}
