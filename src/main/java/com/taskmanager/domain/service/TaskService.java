package com.taskmanager.domain.service;

import com.taskmanager.domain.model.*;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.ProjectRepository;
import com.taskmanager.persistence.repository.TagRepository;
import com.taskmanager.persistence.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TaskService {

    private static final int MAX_OPEN_NO_DUEDATE = 50;

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final TagRepository tagRepo;
    private final ActivityService activitySvc;
    private final DatabaseManager db;

    public TaskService(TaskRepository taskRepo, ProjectRepository projectRepo,
                       TagRepository tagRepo, ActivityService activitySvc, DatabaseManager db) {
        this.taskRepo = taskRepo;
        this.projectRepo = projectRepo;
        this.tagRepo = tagRepo;
        this.activitySvc = activitySvc;
        this.db = db;
    }

    public Task createTask(String title, String description, Priority priority,
                           LocalDate dueDate, Long projectId, Set<Long> tagIds) throws TaskManagerException {
        if (title == null || title.isBlank()) throw new ValidationException("Task title cannot be empty.");
        if (priority == null) throw new ValidationException("Priority is required.");

        if (projectId != null) {
            projectRepo.findById(projectId)
                .orElseThrow(() -> new ValidationException("Project not found: " + projectId));
        }

        return db.withTransaction(() -> {
            if (dueDate == null && taskRepo.countOpenTasksWithoutDueDate() >= MAX_OPEN_NO_DUEDATE) {
                throw new ValidationException(
                    "Cannot create task: system limit of " + MAX_OPEN_NO_DUEDATE +
                    " open tasks without a due date reached."
                );
            }
            Task task = new Task();
            task.setTitle(title.trim());
            task.setDescription(description != null ? description.trim() : null);
            task.setCreationDate(LocalDateTime.now());
            task.setPriority(priority);
            task.setStatus(TaskStatus.OPEN);
            task.setDueDate(dueDate);
            task.setProjectId(projectId);
            taskRepo.save(task);

            if (tagIds != null) {
                for (long tagId : tagIds) {
                    tagRepo.linkTagToTask(tagId, task.getId());
                }
            }
            activitySvc.log(task.getId(), "Task created.");
            return task;
        });
    }

    public Task getById(long id) throws TaskManagerException {
        Task task = taskRepo.findById(id)
            .orElseThrow(() -> new ValidationException("Task not found: " + id));
        task.setTags(tagRepo.findByTaskId(id));
        return task;
    }

    public List<Task> listAll() throws TaskManagerException {
        return taskRepo.findAll();
    }

    public Task updateTitle(long taskId, String title) throws TaskManagerException {
        if (title == null || title.isBlank()) throw new ValidationException("Title cannot be empty.");
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            task.setTitle(title.trim());
            taskRepo.update(task);
            activitySvc.log(taskId, "Title updated to: " + title.trim());
            return task;
        });
    }

    public Task updateDescription(long taskId, String description) throws TaskManagerException {
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            task.setDescription(description != null ? description.trim() : null);
            taskRepo.update(task);
            activitySvc.log(taskId, "Description updated.");
            return task;
        });
    }

    public Task updatePriority(long taskId, Priority priority) throws TaskManagerException {
        if (priority == null) throw new ValidationException("Priority cannot be null.");
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            task.setPriority(priority);
            taskRepo.update(task);
            activitySvc.log(taskId, "Priority updated to: " + priority);
            return task;
        });
    }

    public Task updateDueDate(long taskId, LocalDate dueDate) throws TaskManagerException {
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            // If removing due date, check the 50 limit
            if (dueDate == null && task.getDueDate() != null
                    && task.getStatus() == TaskStatus.OPEN
                    && taskRepo.countOpenTasksWithoutDueDate() >= MAX_OPEN_NO_DUEDATE) {
                throw new ValidationException(
                    "Cannot remove due date: system limit of " + MAX_OPEN_NO_DUEDATE +
                    " open tasks without a due date reached."
                );
            }
            task.setDueDate(dueDate);
            taskRepo.update(task);
            activitySvc.log(taskId, "Due date updated to: " + (dueDate != null ? dueDate : "none"));
            return task;
        });
    }

    public Task updateProject(long taskId, Long projectId) throws TaskManagerException {
        if (projectId != null) {
            projectRepo.findById(projectId)
                .orElseThrow(() -> new ValidationException("Project not found: " + projectId));
        }
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            task.setProjectId(projectId);
            taskRepo.update(task);
            activitySvc.log(taskId, projectId != null
                ? "Moved to project id: " + projectId
                : "Removed from project.");
            return task;
        });
    }

    public Task updateTags(long taskId, Set<Long> tagIds) throws TaskManagerException {
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            tagRepo.unlinkAllTagsFromTask(taskId);
            if (tagIds != null) {
                for (long tagId : tagIds) {
                    tagRepo.linkTagToTask(tagId, taskId);
                }
            }
            activitySvc.log(taskId, "Tags updated.");
            task.setTags(tagRepo.findByTaskId(taskId));
            return task;
        });
    }

    public Task completeTask(long taskId) throws TaskManagerException {
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            if (task.getStatus() == TaskStatus.COMPLETED) {
                throw new ValidationException("Task is already completed.");
            }
            task.setStatus(TaskStatus.COMPLETED);
            taskRepo.update(task);
            activitySvc.log(taskId, "Task completed.");
            return task;
        });
    }

    public Task cancelTask(long taskId) throws TaskManagerException {
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            if (task.getStatus() == TaskStatus.CANCELLED) {
                throw new ValidationException("Task is already cancelled.");
            }
            task.setStatus(TaskStatus.CANCELLED);
            taskRepo.update(task);
            activitySvc.log(taskId, "Task cancelled.");
            return task;
        });
    }

    public Task reopenTask(long taskId) throws TaskManagerException {
        return db.withTransaction(() -> {
            Task task = requireTask(taskId);
            if (task.getStatus() == TaskStatus.OPEN) {
                throw new ValidationException("Task is already open.");
            }
            // If no due date, check limit before reopening
            if (task.getDueDate() == null
                    && taskRepo.countOpenTasksWithoutDueDate() >= MAX_OPEN_NO_DUEDATE) {
                throw new ValidationException(
                    "Cannot reopen task: system limit of " + MAX_OPEN_NO_DUEDATE +
                    " open tasks without a due date reached."
                );
            }
            task.setStatus(TaskStatus.OPEN);
            taskRepo.update(task);
            activitySvc.log(taskId, "Task reopened.");
            return task;
        });
    }

    public void deleteTask(long taskId) throws TaskManagerException {
        requireTask(taskId);
        taskRepo.delete(taskId);
    }

    // Used by RecurrenceService to set recurrence pattern id after task creation
    public void setRecurrencePatternId(long taskId, long patternId) throws TaskManagerException {
        Task task = requireTask(taskId);
        task.setRecurrencePatternId(patternId);
        taskRepo.update(task);
    }

    private Task requireTask(long taskId) throws TaskManagerException {
        return taskRepo.findById(taskId)
            .orElseThrow(() -> new ValidationException("Task not found: " + taskId));
    }
}
