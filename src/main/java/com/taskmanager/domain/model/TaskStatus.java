package com.taskmanager.domain.model;

public enum TaskStatus {
    OPEN, COMPLETED, CANCELLED;

    public static TaskStatus fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
