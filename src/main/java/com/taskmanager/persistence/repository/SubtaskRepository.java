package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.Subtask;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.Optional;

public interface SubtaskRepository {
    Subtask save(Subtask subtask) throws TaskManagerException;
    Optional<Subtask> findById(long id) throws TaskManagerException;
    List<Subtask> findByTaskId(long taskId) throws TaskManagerException;
    int countByTaskId(long taskId) throws TaskManagerException;
    void update(Subtask subtask) throws TaskManagerException;
    void delete(long id) throws TaskManagerException;
}
