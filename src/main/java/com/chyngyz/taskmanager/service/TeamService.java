package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.TeamRequest;
import com.chyngyz.taskmanager.dto.TeamResponse;
import com.chyngyz.taskmanager.entity.Team;
import com.chyngyz.taskmanager.entity.TeamMember;
import com.chyngyz.taskmanager.entity.User;
import com.chyngyz.taskmanager.repository.TeamMemberRepository;
import com.chyngyz.taskmanager.repository.TeamRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamResponse createTeam(TeamRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Creating team by user: {}", username);

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User '{}' not found", username);
                    return new NoSuchElementException("User not found");
                });

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
            team.setMembers(members);
            logger.info("Adding {} members to team '{}'", members.size(), request.getName());
        }

        team = teamRepository.save(team);
        logger.info("Team '{}' created successfully with ID {}", team.getName(), team.getId());

        return toResponse(team);
    }

    public List<TeamResponse> getAllTeams() {
        logger.info("Fetching all teams");
        return teamRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TeamResponse getTeamById(Long id) {
        logger.info("Fetching team by ID {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Team with ID {} not found", id);
                    return new NoSuchElementException("Team not found");
                });
        return toResponse(team);
    }

    public TeamResponse updateTeam(Long id, TeamRequest request) {
        logger.info("Updating team with ID {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Team with ID {} not found", id);
                    return new NoSuchElementException("Team not found");
                });

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        if (request.getMemberIds() != null) {
            Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
            team.setMembers(members);
            logger.info("Updated members of team ID {} with {} users", id, members.size());
        }

        team = teamRepository.save(team);
        logger.info("Team with ID {} updated successfully", id);
        return toResponse(team);
    }

    public void deleteTeam(Long id) {
        logger.info("Deleting team with ID {}", id);
        teamRepository.deleteById(id);
        logger.info("Team with ID {} deleted", id);
    }

    public void addMembers(Long teamId, Set<Long> userIds) {
        logger.info("Adding members {} to team ID {}", userIds, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> {
                    logger.error("Team with ID {} not found", teamId);
                    return new NoSuchElementException("Team not found");
                });

        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            boolean exists = teamMemberRepository.existsByTeamAndUser(team, user);
            if (!exists) {
                TeamMember member = TeamMember.builder()
                        .team(team)
                        .user(user)
                        .joinedAt(LocalDateTime.now())
                        .build();
                teamMemberRepository.save(member);
                logger.info("User ID {} added to team ID {}", user.getId(), teamId);
            } else {
                logger.warn("User ID {} already in team ID {}", user.getId(), teamId);
            }
        }
    }

    public void removeMember(Long id, Long userId) {
        logger.info("Removing user ID {} from team ID {}", userId, id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Team with ID {} not found", id);
                    return new NoSuchElementException("Team not found");
                });

        boolean removed = team.getMembers().removeIf(user -> user.getId().equals(userId));
        if (removed) {
            teamRepository.save(team);
            logger.info("User ID {} removed from team ID {}", userId, id);
        } else {
            logger.warn("User ID {} was not a member of team ID {}", userId, id);
        }
    }

    private TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdByUsername(team.getCreatedBy() != null ? team.getCreatedBy().getUsername() : null)
                .memberUsernames(team.getMembers().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toSet()))
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
