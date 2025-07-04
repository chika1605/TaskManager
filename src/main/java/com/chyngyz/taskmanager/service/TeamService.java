package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.*;
import com.chyngyz.taskmanager.entity.*;
import com.chyngyz.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamResponse createTeam(TeamRequest request) {
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());

        // назначаем менеджера
        if (request.getManagerId() != null) {
            team.setManager(userRepository.findById(request.getManagerId()).orElse(null));
        }

        // добавляем участников
        if (request.getMemberIds() != null) {
            Set<User> members = request.getMemberIds().stream()
                    .map(id -> userRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            team.setMembers(members);
        }

        team = teamRepository.save(team);
        return toResponse(team);
    }

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id).orElseThrow();
        return toResponse(team);
    }

    // и т.д. (обновление, удаление, добавление/удаление участников)

    private TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .managerUsername(team.getManager() != null ? team.getManager().getUsername() : null)
                .memberUsernames(team.getMembers().stream().map(User::getUsername).collect(Collectors.toSet()))
                .build();
    }

    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        // Меняем менеджера, если передан
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new NoSuchElementException("Manager not found"));
            team.setManager(manager);
        }

        // Обновляем участников, если переданы
        if (request.getMemberIds() != null) {
            Set<User> members = request.getMemberIds().stream()
                    .map(userId -> userRepository.findById(userId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            team.setMembers(members);
        }

        team = teamRepository.save(team);
        return toResponse(team);
    }


    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }

    public void addMembers(Long id, List<Long> userIds) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        Set<User> members = team.getMembers();
        for (Long userId : userIds) {
            userRepository.findById(userId).ifPresent(members::add);
        }
        team.setMembers(members);
        teamRepository.save(team);
    }

    public void removeMember(Long id, Long userId) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Team not found"));
        team.getMembers().removeIf(user -> user.getId().equals(userId));
        teamRepository.save(team);
    }

}
