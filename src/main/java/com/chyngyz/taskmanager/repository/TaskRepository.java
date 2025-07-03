package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.Task;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUser(User user);
}
