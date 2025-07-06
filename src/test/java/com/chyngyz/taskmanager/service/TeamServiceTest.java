package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.TeamRequest;
import com.chyngyz.taskmanager.dto.TeamResponse;
import com.chyngyz.taskmanager.entity.Team;
import com.chyngyz.taskmanager.entity.User;
import com.chyngyz.taskmanager.repository.TeamMemberRepository;
import com.chyngyz.taskmanager.repository.TeamRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Подмена SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createTeam_shouldReturnTeamResponse() {
        User creator = User.builder().id(1L).username("testuser").build();
        TeamRequest request = new TeamRequest();
        request.setName("Team A");
        request.setDescription("Test team");
        request.setMemberIds(Set.of(2L, 3L));

        User user2 = User.builder().id(2L).username("member1").build();
        User user3 = User.builder().id(3L).username("member2").build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(creator));
        when(userRepository.findAllById(Set.of(2L, 3L))).thenReturn(List.of(user2, user3));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.createTeam(request);

        assertEquals("Team A", response.getName());
        assertEquals("Test team", response.getDescription());
        assertTrue(response.getMemberUsernames().contains("member1"));
        assertTrue(response.getMemberUsernames().contains("member2"));
    }

    @Test
    void getTeamById_shouldReturnTeam() {
        Team team = Team.builder().id(1L).name("Team A").description("desc").members(new HashSet<>()).build();
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.getTeamById(1L);
        assertEquals("Team A", response.getName());
    }

    @Test
    void addMembers_shouldAddNewUsersOnly() {
        Team team = Team.builder().id(1L).name("T").members(new HashSet<>()).build();
        User user = User.builder().id(2L).username("u").build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findAllById(Set.of(2L))).thenReturn(List.of(user));
        when(teamMemberRepository.existsByTeamAndUser(team, user)).thenReturn(false);

        teamService.addMembers(1L, Set.of(2L));

        verify(teamMemberRepository).save(any());
    }

    @Test
    void removeMember_shouldRemoveUserFromTeam() {
        User user = User.builder().id(2L).username("u").build();
        Set<User> members = new HashSet<>();
        members.add(user);
        Team team = Team.builder().id(1L).members(members).build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        teamService.removeMember(1L, 2L);

        assertFalse(team.getMembers().contains(user));
        verify(teamRepository).save(team);
    }

    @Test
    void getAllTeams_shouldReturnTeamResponses() {
        Team team1 = Team.builder().id(1L).name("Team A").members(Set.of()).build();
        Team team2 = Team.builder().id(2L).name("Team B").members(Set.of()).build();

        when(teamRepository.findAll()).thenReturn(List.of(team1, team2));

        List<TeamResponse> teams = teamService.getAllTeams();

        assertEquals(2, teams.size());
        assertEquals("Team A", teams.get(0).getName());
        assertEquals("Team B", teams.get(1).getName());
    }

    @Test
    void updateTeam_shouldUpdateAndReturnResponse() {
        User member = User.builder().id(2L).username("updatedUser").build();
        Team team = Team.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Desc")
                .members(new HashSet<>())
                .build();

        TeamRequest request = new TeamRequest();
        request.setName("New Name");
        request.setDescription("New Desc");
        request.setMemberIds(Set.of(2L));

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findAllById(Set.of(2L))).thenReturn(List.of(member));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamResponse response = teamService.updateTeam(1L, request);

        assertEquals("New Name", response.getName());
        assertEquals("New Desc", response.getDescription());
        assertTrue(response.getMemberUsernames().contains("updatedUser"));
    }

    @Test
    void deleteTeam_shouldCallRepositoryDelete() {
        doNothing().when(teamRepository).deleteById(1L);

        teamService.deleteTeam(1L);

        verify(teamRepository).deleteById(1L);
    }
}
