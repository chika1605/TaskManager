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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;

    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found");
                });
        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found");
                });

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        logger.info("User updated: {}", user.getUsername());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.warn("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found");
                });

        List<Task> tasks = taskRepository.findAllByAssignedTo(user);
        for (Task task : tasks) {
            task.setAssignedTo(null);
        }
        taskRepository.saveAll(tasks);
        logger.info("Cleared user assignments from tasks");

        List<Team> teams = teamRepository.findAllByMembersContains(user);
        for (Team team : teams) {
            team.getMembers().remove(user);
        }
        teamRepository.saveAll(teams);
        logger.info("Removed user from teams");

        refreshTokenRepository.deleteAllByUser(user);
        logger.info("Deleted user refresh tokens");

        userRepository.delete(user);
        logger.info("User deleted: {}", user.getUsername());
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
