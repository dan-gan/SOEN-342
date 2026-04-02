package com.taskmanager.domain.service;

import com.taskmanager.domain.model.ActivityEntry;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.repository.ActivityEntryRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityService {

    private final ActivityEntryRepository activityRepo;

    public ActivityService(ActivityEntryRepository activityRepo) {
        this.activityRepo = activityRepo;
    }

    public void log(long taskId, String description) throws TaskManagerException {
        ActivityEntry entry = new ActivityEntry();
        entry.setTaskId(taskId);
        entry.setTimestamp(LocalDateTime.now());
        entry.setDescription(description);
        activityRepo.save(entry);
    }

    public List<ActivityEntry> getHistory(long taskId) throws TaskManagerException {
        return activityRepo.findByTaskId(taskId);
    }
}
