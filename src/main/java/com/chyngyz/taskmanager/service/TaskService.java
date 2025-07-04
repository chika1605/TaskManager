package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.TaskRequest;
import com.chyngyz.taskmanager.dto.TaskResponse;
import com.chyngyz.taskmanager.entity.Task;
import com.chyngyz.taskmanager.entity.TaskStatus;
import com.chyngyz.taskmanager.entity.User;
import com.chyngyz.taskmanager.repository.TaskRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public void createTask(TaskRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setUser(user);

        taskRepository.save(task);
    }

    public void updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Task not found"));

        taskRepository.delete(task);
    }

    public Page<TaskResponse> getTasks(int page, int size, String status, Long userId) {
        Pageable pageable = PageRequest.of(page, size);

        // Получаем текущего пользователя и его роль
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String role = currentUser.getRole().name();

        // Если роль USER — показываем только его задачи, иначе — все
        if (role.equals("USER")) {
            return taskRepository.findByUser(currentUser, pageable)
                    .map(this::toResponse);
        } else {
            // Можно фильтровать по статусу и по userId (если надо)
            if (status != null && userId != null) {
                return taskRepository.findByStatusAndUserId(TaskStatus.valueOf(status), userId, pageable)
                        .map(this::toResponse);
            }
            if (status != null) {
                return taskRepository.findByStatus(TaskStatus.valueOf(status), pageable)
                        .map(this::toResponse);
            }
            if (userId != null) {
                return taskRepository.findByUserId(userId, pageable)
                        .map(this::toResponse);
            }
            // Без фильтра
            return taskRepository.findAll(pageable).map(this::toResponse);
        }
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        return toResponse(task);
    }

    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(status);
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        task.setUser(user);
        return toResponse(taskRepository.save(task));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .userId(task.getUser() != null ? task.getUser().getId() : null)
                .build();
    }

}
