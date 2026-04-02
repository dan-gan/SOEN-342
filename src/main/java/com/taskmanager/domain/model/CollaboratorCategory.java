package com.taskmanager.domain.model;

public enum CollaboratorCategory {
    SENIOR(2), INTERMEDIATE(5), JUNIOR(10);

    private final int openTaskLimit;

    CollaboratorCategory(int openTaskLimit) {
        this.openTaskLimit = openTaskLimit;
    }

    public int getOpenTaskLimit() {
        return openTaskLimit;
    }

    public static CollaboratorCategory fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
