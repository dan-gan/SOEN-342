package com.taskmanager.domain.model;

public enum Priority {
    LOW, MEDIUM, HIGH;

    public static Priority fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
