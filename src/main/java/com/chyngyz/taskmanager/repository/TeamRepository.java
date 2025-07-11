package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.Team;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByMembersContains(User user);
}
