package com.chyngyz.taskmanager.dto;

import com.chyngyz.taskmanager.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private Integer priority;
    private String category;
    private Long assignedToId;
    private Long teamId;
    private LocalDateTime deadline;
}
