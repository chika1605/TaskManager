package com.chyngyz.taskmanager.dto;

import com.chyngyz.taskmanager.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer priority;
    private String category;
    private Long createdById;
    private Long assignedToId;
    private Long teamId;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
