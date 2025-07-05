package com.chyngyz.taskmanager.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private String createdByUsername;
    private Set<String> memberUsernames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
