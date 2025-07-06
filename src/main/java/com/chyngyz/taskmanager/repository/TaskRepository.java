package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.Task;
import com.chyngyz.taskmanager.entity.TaskStatus;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByCategory(String category, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByAssignedToId(Long userId, Pageable pageable);

    Page<Task> findByTeamId(Long teamId, Pageable pageable);

    Page<Task> findByCreatedById(Long userId, Pageable pageable);

    Page<Task> findByPriority(Integer priority, Pageable pageable);

    Page<Task> findByCategoryAndStatus(String category, TaskStatus status, Pageable pageable);

    Page<Task> findAll(Pageable pageable);

    List<Task> findAllByAssignedTo(User user);
}
