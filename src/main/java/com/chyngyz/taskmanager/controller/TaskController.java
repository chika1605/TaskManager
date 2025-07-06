package com.chyngyz.taskmanager.controller;

import com.chyngyz.taskmanager.dto.TaskRequest;
import com.chyngyz.taskmanager.dto.TaskResponse;
import com.chyngyz.taskmanager.entity.TaskStatus;
import com.chyngyz.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        logger.info("Creating task with title: {}", request.getTitle());
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @GetMapping
    public Page<TaskResponse> getTasks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long createdById,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        logger.info("Fetching tasks with filters: category={}, status={}, priority={}, teamId={}", category, status, priority, teamId);
        return taskService.getTasks(category, status, priority, assignedToId, createdById, teamId, page, size, sort);
    }


    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        logger.info("Updating task with id: {}", id);
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        logger.info("Deleting task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted");
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        logger.info("Getting task by id: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        logger.info("Updating task status. id: {}, new status: {}", id, status);
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        logger.info("Assigning task id: {} to userId: {}", id, userId);
        return ResponseEntity.ok(taskService.assignTask(id, userId));
    }
}
