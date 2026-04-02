package com.taskmanager.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Task {
    private long id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private Priority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private Long projectId;
    private Long recurrencePatternId;

    // Loaded eagerly when needed
    private List<Subtask> subtasks = new ArrayList<>();
    private Set<Tag> tags = new HashSet<>();
    private List<ActivityEntry> activityHistory = new ArrayList<>();

    public Task() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getRecurrencePatternId() { return recurrencePatternId; }
    public void setRecurrencePatternId(Long recurrencePatternId) { this.recurrencePatternId = recurrencePatternId; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public List<ActivityEntry> getActivityHistory() { return activityHistory; }
    public void setActivityHistory(List<ActivityEntry> activityHistory) { this.activityHistory = activityHistory; }
}
