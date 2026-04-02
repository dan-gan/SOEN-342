package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Subtask;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.repository.SubtaskRepository;
import com.taskmanager.persistence.repository.TaskRepository;

import java.util.List;

public class SubtaskService {

    private static final int MAX_SUBTASKS = 20;

    private final SubtaskRepository subtaskRepo;
    private final TaskRepository taskRepo;
    private final ActivityService activitySvc;

    public SubtaskService(SubtaskRepository subtaskRepo, TaskRepository taskRepo, ActivityService activitySvc) {
        this.subtaskRepo = subtaskRepo;
        this.taskRepo = taskRepo;
        this.activitySvc = activitySvc;
    }

    public Subtask addSubtask(long taskId, String title) throws TaskManagerException {
        if (title == null || title.isBlank()) throw new ValidationException("Subtask title cannot be empty.");
        taskRepo.findById(taskId).orElseThrow(() -> new ValidationException("Task not found: " + taskId));

        if (subtaskRepo.countByTaskId(taskId) >= MAX_SUBTASKS) {
            throw new ValidationException("Task already has the maximum of " + MAX_SUBTASKS + " subtasks.");
        }
        Subtask subtask = new Subtask();
        subtask.setTitle(title.trim());
        subtask.setCompleted(false);
        subtask.setTaskId(taskId);
        subtaskRepo.save(subtask);
        activitySvc.log(taskId, "Subtask added: " + title.trim());
        return subtask;
    }

    public Subtask completeSubtask(long subtaskId) throws TaskManagerException {
        Subtask subtask = requireSubtask(subtaskId);
        if (subtask.isCompleted()) throw new ValidationException("Subtask is already completed.");
        subtask.setCompleted(true);
        subtaskRepo.update(subtask);
        activitySvc.log(subtask.getTaskId(), "Subtask completed: " + subtask.getTitle());
        return subtask;
    }

    public Subtask reopenSubtask(long subtaskId) throws TaskManagerException {
        Subtask subtask = requireSubtask(subtaskId);
        if (!subtask.isCompleted()) throw new ValidationException("Subtask is not completed.");
        subtask.setCompleted(false);
        subtaskRepo.update(subtask);
        activitySvc.log(subtask.getTaskId(), "Subtask reopened: " + subtask.getTitle());
        return subtask;
    }

    public void deleteSubtask(long subtaskId) throws TaskManagerException {
        Subtask subtask = requireSubtask(subtaskId);
        subtaskRepo.delete(subtaskId);
        activitySvc.log(subtask.getTaskId(), "Subtask deleted: " + subtask.getTitle());
    }

    public List<Subtask> listByTask(long taskId) throws TaskManagerException {
        return subtaskRepo.findByTaskId(taskId);
    }

    public Subtask getById(long subtaskId) throws TaskManagerException {
        return requireSubtask(subtaskId);
    }

    /** Used internally by CollaboratorService without activity log to avoid double-logging. */
    Subtask addSubtaskInternal(long taskId, String title) throws TaskManagerException {
        if (subtaskRepo.countByTaskId(taskId) >= MAX_SUBTASKS) {
            throw new ValidationException("Task already has the maximum of " + MAX_SUBTASKS + " subtasks.");
        }
        Subtask subtask = new Subtask();
        subtask.setTitle(title.trim());
        subtask.setCompleted(false);
        subtask.setTaskId(taskId);
        return subtaskRepo.save(subtask);
    }

    private Subtask requireSubtask(long subtaskId) throws TaskManagerException {
        return subtaskRepo.findById(subtaskId)
            .orElseThrow(() -> new ValidationException("Subtask not found: " + subtaskId));
    }
}
