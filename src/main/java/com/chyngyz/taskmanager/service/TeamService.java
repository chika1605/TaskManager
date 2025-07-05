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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    // Создать команду (creator берём из текущего пользователя)
    public TeamResponse createTeam(TeamRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<User> members = userRepository.findAllById(request.getMemberIds())
                    .stream().collect(Collectors.toSet());
            team.setMembers(members);
        }

        team = teamRepository.save(team);
        return toResponse(team);
    }

    // Получить все команды
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Получить команду по ID
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        return toResponse(team);
    }

    // Обновить команду
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        if (request.getMemberIds() != null) {
            Set<User> members = userRepository.findAllById(request.getMemberIds())
                    .stream().collect(Collectors.toSet());
            team.setMembers(members);
        }

        team = teamRepository.save(team);
        return toResponse(team);
    }

    // Удалить команду
    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }

    // Добавить участников в команду
    public void addMembers(Long teamId, Set<Long> userIds) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            // Проверяем, не состоит ли уже пользователь в команде (чтобы не дублировать)
            boolean exists = teamMemberRepository.existsByTeamAndUser(team, user);
            if (!exists) {
                TeamMember member = TeamMember.builder()
                        .team(team)
                        .user(user)
                        .joinedAt(LocalDateTime.now())
                        .build();
                teamMemberRepository.save(member);
            }
        }
    }

    // Удалить участника из команды
    public void removeMember(Long id, Long userId) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        team.getMembers().removeIf(user -> user.getId().equals(userId));
        teamRepository.save(team);
    }

    // Маппинг в DTO
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
