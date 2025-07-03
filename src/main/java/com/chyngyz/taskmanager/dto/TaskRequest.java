package com.chyngyz.taskmanager.dto;

import com.chyngyz.taskmanager.entity.TaskStatus;
import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
}
