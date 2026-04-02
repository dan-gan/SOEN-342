package com.taskmanager.cli;

import com.taskmanager.domain.service.CollaboratorOverloadWarning;
import com.taskmanager.domain.service.CollaboratorService;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;

public class OverloadMenu {

    private final CollaboratorService collaboratorSvc;

    public OverloadMenu(CollaboratorService collaboratorSvc) {
        this.collaboratorSvc = collaboratorSvc;
    }

    public void show() {
        try {
            List<CollaboratorOverloadWarning> warnings = collaboratorSvc.detectOverloadedCollaborators();
            ConsoleUtils.println("\n--- Overloaded Collaborators ---");
            if (warnings.isEmpty()) {
                ConsoleUtils.println("  No overloaded collaborators.");
            } else {
                for (CollaboratorOverloadWarning w : warnings) {
                    ConsoleUtils.printWarning(w.toString());
                }
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }
}
