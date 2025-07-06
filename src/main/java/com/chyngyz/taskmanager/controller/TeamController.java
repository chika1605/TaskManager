package com.chyngyz.taskmanager.controller;

import com.chyngyz.taskmanager.dto.TeamRequest;
import com.chyngyz.taskmanager.dto.TeamResponse;
import com.chyngyz.taskmanager.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@RequestBody TeamRequest request) {
        logger.info("Creating team with name: {}", request.getName());
        return ResponseEntity.ok(teamService.createTeam(request));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        logger.info("Fetching all teams");
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        logger.info("Fetching team by id: {}", id);
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long id, @RequestBody TeamRequest request) {
        logger.info("Updating team id: {} with name: {}", id, request.getName());
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        logger.info("Deleting team with id: {}", id);
        teamService.deleteTeam(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMembersToTeam(
            @PathVariable Long id,
            @RequestBody Set<Long> userIds
    ) {
        logger.info("Adding members to team id: {} -> userIds: {}", id, userIds);
        teamService.addMembers(id, userIds);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        logger.info("Removing user id: {} from team id: {}", userId, id);
        teamService.removeMember(id, userId);
        return ResponseEntity.ok().build();
    }
}
