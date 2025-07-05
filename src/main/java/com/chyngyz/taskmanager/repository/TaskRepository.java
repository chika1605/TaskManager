package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.Task;
import com.chyngyz.taskmanager.entity.TaskStatus;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Все задачи по категории (с пагинацией и сортировкой)
    Page<Task> findByCategory(String category, Pageable pageable);

    // Все задачи по статусу
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    // Все задачи по исполнителю (assignedTo)
    Page<Task> findByAssignedToId(Long userId, Pageable pageable);

    // Все задачи по команде
    Page<Task> findByTeamId(Long teamId, Pageable pageable);

    // Все задачи по создателю
    Page<Task> findByCreatedById(Long userId, Pageable pageable);

    // Все задачи с определённым приоритетом
    Page<Task> findByPriority(Integer priority, Pageable pageable);

    // Можно комбинировать фильтры (категория + статус)
    Page<Task> findByCategoryAndStatus(String category, TaskStatus status, Pageable pageable);

    // Найти всё (с пагинацией и сортировкой)
    Page<Task> findAll(Pageable pageable);

    List<Task> findAllByAssignedTo(User user);
}
