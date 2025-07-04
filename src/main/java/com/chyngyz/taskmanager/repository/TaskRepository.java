package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.Task;
import com.chyngyz.taskmanager.entity.TaskStatus;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUser(User user);
    Page<Task> findByUser(User user, Pageable pageable);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByUserId(Long userId, Pageable pageable);
    Page<Task> findByStatusAndUserId(TaskStatus status, Long userId, Pageable pageable);
}
