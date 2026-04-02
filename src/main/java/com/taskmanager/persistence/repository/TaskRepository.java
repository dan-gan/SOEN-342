package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task) throws TaskManagerException;
    Optional<Task> findById(long id) throws TaskManagerException;
    List<Task> findAll() throws TaskManagerException;
    List<Task> findByProjectId(long projectId) throws TaskManagerException;
    void update(Task task) throws TaskManagerException;
    void delete(long id) throws TaskManagerException;
    int countOpenTasksWithoutDueDate() throws TaskManagerException;
    boolean existsByTitleAndDueDate(String title, String dueDate) throws TaskManagerException;
    List<Task> findByStatus(TaskStatus status) throws TaskManagerException;
    List<Task> findByRecurrencePatternId(long recurrencePatternId) throws TaskManagerException;
}
