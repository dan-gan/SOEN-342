package com.taskmanager.domain.model;

import java.time.LocalDateTime;

public class ActivityEntry {
    private long id;
    private long taskId;
    private LocalDateTime timestamp;
    private String description;

    public ActivityEntry() {}

    public ActivityEntry(long id, long taskId, LocalDateTime timestamp, String description) {
        this.id = id;
        this.taskId = taskId;
        this.timestamp = timestamp;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
