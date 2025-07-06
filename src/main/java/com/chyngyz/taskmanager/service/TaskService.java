package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.TaskRequest;
import com.chyngyz.taskmanager.dto.TaskResponse;
import com.chyngyz.taskmanager.entity.*;
import com.chyngyz.taskmanager.repository.TaskRepository;
import com.chyngyz.taskmanager.repository.TeamRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public TaskResponse createTask(TaskRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Creating task by user: {}", username);

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User '{}' not found", username);
                    return new UsernameNotFoundException("User not found");
                });

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> {
                        logger.error("Assigned user with ID {} not found", request.getAssignedToId());
                        return new EntityNotFoundException("Assigned user not found");
                    });
        }

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId()).orElse(null);
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .category(request.getCategory())
                .createdBy(creator)
                .assignedTo(assignedTo)
                .team(team)
                .deadline(request.getDeadline())
                .build();

        taskRepository.save(task);
        logger.info("Task created: {}", task.getTitle());

        return toResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        logger.info("Updating task with ID {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Task with ID {} not found", id);
                    return new EntityNotFoundException("Task not found");
                });

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setCategory(request.getCategory());
        task.setDeadline(request.getDeadline());

        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> {
                        logger.error("Assigned user with ID {} not found", request.getAssignedToId());
                        return new EntityNotFoundException("Assigned user not found");
                    });
            task.setAssignedTo(assignedTo);
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId()).orElse(null);
            task.setTeam(team);
        }

        logger.info("Task with ID {} updated successfully", id);
        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        logger.info("Deleting task with ID {}", id);
        taskRepository.deleteById(id);
    }

    public Page<TaskResponse> getTasks(
            String category, TaskStatus status, Integer priority,
            Long assignedToId, Long createdById, Long teamId,
            int page, int size, String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        logger.info("Fetching tasks with filters: category={}, status={}, priority={}, assignedToId={}, createdById={}, teamId={}",
                category, status, priority, assignedToId, createdById, teamId);

        Page<Task> tasks;

        if (category != null && status != null) {
            tasks = taskRepository.findByCategoryAndStatus(category, status, pageable);
        } else if (category != null) {
            tasks = taskRepository.findByCategory(category, pageable);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            tasks = taskRepository.findByPriority(priority, pageable);
        } else if (assignedToId != null) {
            tasks = taskRepository.findByAssignedToId(assignedToId, pageable);
        } else if (createdById != null) {
            tasks = taskRepository.findByCreatedById(createdById, pageable);
        } else if (teamId != null) {
            tasks = taskRepository.findByTeamId(teamId, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        logger.info("Found {} tasks", tasks.getTotalElements());
        return tasks.map(this::toResponse);
    }

    public TaskResponse getTaskById(Long id) {
        logger.info("Fetching task by ID {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Task with ID {} not found", id);
                    return new EntityNotFoundException("Task not found");
                });

        return toResponse(task);
    }

    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        logger.info("Updating status of task ID {} to {}", id, status);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(status);
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse assignTask(Long taskId, Long userId) {
        logger.info("Assigning task ID {} to user ID {}", taskId, userId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        task.setAssignedTo(user);
        return toResponse(taskRepository.save(task));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .category(task.getCategory())
                .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .teamId(task.getTeam() != null ? task.getTeam().getId() : null)
                .deadline(task.getDeadline())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
