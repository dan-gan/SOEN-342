package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Collaborator;

public class CollaboratorOverloadWarning {
    private final Collaborator collaborator;
    private final int openTaskCount;

    public CollaboratorOverloadWarning(Collaborator collaborator, int openTaskCount) {
        this.collaborator = collaborator;
        this.openTaskCount = openTaskCount;
    }

    public Collaborator getCollaborator() { return collaborator; }
    public int getOpenTaskCount() { return openTaskCount; }
    public int getLimit() { return collaborator.getCategory().getOpenTaskLimit(); }

    @Override
    public String toString() {
        return collaborator.getName() + " (" + collaborator.getCategory() + "): " +
               openTaskCount + "/" + getLimit() + " open tasks";
    }
}
