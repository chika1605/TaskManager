package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.Team;
import com.chyngyz.taskmanager.entity.TeamMember;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByTeamAndUser(Team team, User user);

}
