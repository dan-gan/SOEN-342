package com.taskmanager.cli;

import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.service.ProjectService;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectMenu {

    private final ProjectService projectSvc;

    public ProjectMenu(ProjectService projectSvc) {
        this.projectSvc = projectSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Projects ---");
            ConsoleUtils.println("1. Create project");
            ConsoleUtils.println("2. List all projects");
            ConsoleUtils.println("3. Update project");
            ConsoleUtils.println("4. Delete project");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 4);
            switch (choice) {
                case 1 -> handleCreate();
                case 2 -> handleList();
                case 3 -> handleUpdate();
                case 4 -> handleDelete();
                case 0 -> { return; }
            }
        }
    }

    private void handleCreate() {
        try {
            String name = ConsoleUtils.readString("Project name: ");
            String desc = ConsoleUtils.readOptionalString("Description (optional): ");
            Project p = projectSvc.createProject(name, desc.isBlank() ? null : desc);
            ConsoleUtils.printSuccess("Project created: [" + p.getId() + "] " + p.getName());
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleList() {
        try {
            List<Project> projects = projectSvc.listAll();
            ConsoleUtils.printTable(
                List.of("ID", "Name", "Description"),
                projects.stream().map(p -> List.of(
                    String.valueOf(p.getId()), p.getName(), nvl(p.getDescription())
                )).collect(Collectors.toList())
            );
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleUpdate() {
        try {
            long id = ConsoleUtils.readInt("Project ID: ");
            String name = ConsoleUtils.readOptionalString("New name (blank to keep): ");
            String desc = ConsoleUtils.readOptionalString("New description (blank to keep): ");
            projectSvc.updateProject(id, name.isBlank() ? null : name, desc.isBlank() ? null : desc);
            ConsoleUtils.printSuccess("Project updated.");
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleDelete() {
        try {
            long id = ConsoleUtils.readInt("Project ID to delete: ");
            if (ConsoleUtils.readYesNo("Are you sure?")) {
                projectSvc.deleteProject(id);
                ConsoleUtils.printSuccess("Project deleted.");
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
