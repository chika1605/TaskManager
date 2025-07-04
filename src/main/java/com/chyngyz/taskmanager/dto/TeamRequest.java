package com.chyngyz.taskmanager.dto;

import lombok.Data;

import java.util.Set;

@Data
public class TeamRequest {
    private String name;
    private String description;
    private Long managerId; // если нужен
    private Set<Long> memberIds;
}