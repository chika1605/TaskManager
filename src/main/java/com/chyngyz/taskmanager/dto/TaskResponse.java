package com.chyngyz.taskmanager.dto;

import com.chyngyz.taskmanager.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Long userId;
}
