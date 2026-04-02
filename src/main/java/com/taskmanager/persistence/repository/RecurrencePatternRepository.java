package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.RecurrencePattern;
import com.taskmanager.exception.TaskManagerException;

import java.util.Optional;

public interface RecurrencePatternRepository {
    RecurrencePattern save(RecurrencePattern pattern) throws TaskManagerException;
    Optional<RecurrencePattern> findById(long id) throws TaskManagerException;
    void delete(long id) throws TaskManagerException;
}
