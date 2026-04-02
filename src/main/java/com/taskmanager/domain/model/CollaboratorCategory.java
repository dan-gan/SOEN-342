package com.taskmanager.domain.model;

public enum CollaboratorCategory {
    SENIOR(2), INTERMEDIATE(5), JUNIOR(10);

    private final int defaultLimit;
    private static final java.util.EnumMap<CollaboratorCategory, Integer> customLimits =
            new java.util.EnumMap<>(CollaboratorCategory.class);

    CollaboratorCategory(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public int getOpenTaskLimit() {
        return customLimits.getOrDefault(this, defaultLimit);
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    /** OCL: limit must be a positive integer. */
    public static void setLimit(CollaboratorCategory category, int limit) {
        if (limit <= 0) throw new IllegalArgumentException(
                "Limit for " + category + " must be a positive integer, got: " + limit);
        customLimits.put(category, limit);
    }

    public static CollaboratorCategory fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
