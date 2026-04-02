package com.taskmanager.domain.model;

public class Collaborator {
    private long id;
    private String name;
    private CollaboratorCategory category;
    private long projectId;

    public Collaborator() {}

    public Collaborator(long id, String name, CollaboratorCategory category, long projectId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.projectId = projectId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CollaboratorCategory getCategory() { return category; }
    public void setCategory(CollaboratorCategory category) { this.category = category; }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    @Override
    public String toString() { return name + " (" + category + ")"; }
}
