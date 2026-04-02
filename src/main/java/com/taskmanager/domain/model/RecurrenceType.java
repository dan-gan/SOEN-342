package com.taskmanager.domain.model;

public enum RecurrenceType {
    DAILY, WEEKLY, MONTHLY;

    public static RecurrenceType fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
