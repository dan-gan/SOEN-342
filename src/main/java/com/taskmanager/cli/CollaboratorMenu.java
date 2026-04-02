package com.taskmanager.cli;

import com.taskmanager.domain.model.Collaborator;
import com.taskmanager.domain.model.CollaboratorCategory;
import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.service.CollaboratorService;
import com.taskmanager.domain.service.ProjectService;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.stream.Collectors;

public class CollaboratorMenu {

    private final CollaboratorService collaboratorSvc;
    private final ProjectService projectSvc;

    public CollaboratorMenu(CollaboratorService collaboratorSvc, ProjectService projectSvc) {
        this.collaboratorSvc = collaboratorSvc;
        this.projectSvc = projectSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Collaborators ---");
            ConsoleUtils.println("1. Add collaborator to project");
            ConsoleUtils.println("2. List collaborators in project");
            ConsoleUtils.println("3. Assign collaborator to task");
            ConsoleUtils.println("4. Change collaborator category");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 4);
            switch (choice) {
                case 1 -> handleAdd();
                case 2 -> handleList();
                case 3 -> handleAssign();
                case 4 -> handleChangeCategory();
                case 0 -> { return; }
            }
        }
    }

    private void handleAdd() {
        try {
            long projectId = ConsoleUtils.readInt("Project ID: ");
            projectSvc.getById(projectId);
            String name = ConsoleUtils.readString("Collaborator name: ");
            CollaboratorCategory cat = readCategory();
            Collaborator c = collaboratorSvc.addCollaboratorToProject(name, cat, projectId);
            ConsoleUtils.printSuccess("Collaborator added: [" + c.getId() + "] " + c.getName() + " (" + c.getCategory() + ")");
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleList() {
        try {
            long projectId = ConsoleUtils.readInt("Project ID: ");
            List<Collaborator> list = collaboratorSvc.listByProject(projectId);
            ConsoleUtils.printTable(List.of("ID", "Name", "Category"),
                list.stream().map(c -> List.of(
                    String.valueOf(c.getId()), c.getName(), c.getCategory().name()
                )).collect(Collectors.toList()));
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleAssign() {
        try {
            long collaboratorId = ConsoleUtils.readInt("Collaborator ID: ");
            long taskId = ConsoleUtils.readInt("Task ID: ");
            collaboratorSvc.assignCollaboratorToTask(collaboratorId, taskId);
            ConsoleUtils.printSuccess("Collaborator assigned. Subtask auto-created.");
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleChangeCategory() {
        try {
            long collaboratorId = ConsoleUtils.readInt("Collaborator ID: ");
            CollaboratorCategory newCat = readCategory();
            var warnings = collaboratorSvc.changeCategory(collaboratorId, newCat);
            ConsoleUtils.printSuccess("Category updated.");
            if (!warnings.isEmpty()) {
                ConsoleUtils.printWarning("Overloaded collaborators after change:");
                warnings.forEach(w -> ConsoleUtils.println("  " + w));
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private CollaboratorCategory readCategory() {
        ConsoleUtils.println("Category: 1=SENIOR(2)  2=INTERMEDIATE(5)  3=JUNIOR(10)");
        int choice = ConsoleUtils.readInt("Choose: ", 1, 3);
        return switch (choice) { case 1 -> CollaboratorCategory.SENIOR; case 2 -> CollaboratorCategory.INTERMEDIATE; default -> CollaboratorCategory.JUNIOR; };
    }
}
