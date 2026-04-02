package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.ActivityEntry;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;

public interface ActivityEntryRepository {
    ActivityEntry save(ActivityEntry entry) throws TaskManagerException;
    List<ActivityEntry> findByTaskId(long taskId) throws TaskManagerException;
}
