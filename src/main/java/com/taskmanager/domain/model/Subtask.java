package com.taskmanager.domain.model;

public class Subtask {
    private long id;
    private String title;
    private boolean completed;
    private long taskId;

    public Subtask() {}

    public Subtask(long id, String title, boolean completed, long taskId) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.taskId = taskId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }
}
