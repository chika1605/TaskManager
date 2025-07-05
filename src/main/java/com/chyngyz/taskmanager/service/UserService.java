package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.UserRequest;
import com.chyngyz.taskmanager.dto.UserResponse;
import com.chyngyz.taskmanager.entity.Task;
import com.chyngyz.taskmanager.entity.Team;
import com.chyngyz.taskmanager.entity.User;
import com.chyngyz.taskmanager.repository.RefreshTokenRepository;
import com.chyngyz.taskmanager.repository.TaskRepository;
import com.chyngyz.taskmanager.repository.TeamRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Можно добавить: user.setRole(request.getRole()); — если требуется

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 1. Удаляем или отвязываем задачи
        List<Task> tasks = taskRepository.findAllByAssignedTo(user);
        for (Task task : tasks) {
            task.setAssignedTo(null);
        }
        taskRepository.saveAll(tasks);

        // 2. Удаляем пользователя из команд (если связь через Team.members)
        List<Team> teams = teamRepository.findAllByMembersContains(user);
        for (Team team : teams) {
            team.getMembers().remove(user);
        }
        teamRepository.saveAll(teams);

        // 3. Удаляем токены
        refreshTokenRepository.deleteAllByUser(user);

        // 4. Удаляем пользователя
        userRepository.delete(user);
    }


    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
