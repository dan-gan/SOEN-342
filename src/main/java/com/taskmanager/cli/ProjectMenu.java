package com.taskmanager.cli;

import com.taskmanager.domain.model.Collaborator;
import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.service.CollaboratorService;
import com.taskmanager.domain.service.ProjectService;
import com.taskmanager.domain.service.TaskService;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectMenu {

    private final ProjectService projectSvc;
    private final TaskService taskSvc;
    private final CollaboratorService collaboratorSvc;

    public ProjectMenu(ProjectService projectSvc, TaskService taskSvc, CollaboratorService collaboratorSvc) {
        this.projectSvc = projectSvc;
        this.taskSvc = taskSvc;
        this.collaboratorSvc = collaboratorSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Projects ---");
            ConsoleUtils.println("1. Create project");
            ConsoleUtils.println("2. List all projects");
            ConsoleUtils.println("3. View project details");
            ConsoleUtils.println("4. Update project");
            ConsoleUtils.println("5. Delete project");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 5);
            switch (choice) {
                case 1 -> handleCreate();
                case 2 -> handleList();
                case 3 -> handleDetail();
                case 4 -> handleUpdate();
                case 5 -> handleDelete();
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
            List<List<String>> rows = projects.stream().map(p -> {
                String tasks = "", open = "", collaborators = "";
                try {
                    List<Task> pts = taskSvc.listAll().stream()
                        .filter(t -> p.getId() == (t.getProjectId() != null ? t.getProjectId() : -1))
                        .collect(Collectors.toList());
                    long openCount = pts.stream().filter(t -> t.getStatus() == TaskStatus.OPEN).count();
                    tasks = String.valueOf(pts.size());
                    open  = String.valueOf(openCount);
                } catch (TaskManagerException ignored) {}
                try {
                    collaborators = String.valueOf(collaboratorSvc.listByProject(p.getId()).size());
                } catch (TaskManagerException ignored) {}
                return List.of(String.valueOf(p.getId()), p.getName(), tasks, open, collaborators, nvl(p.getDescription()));
            }).collect(Collectors.toList());
            ConsoleUtils.printTable(List.of("ID", "Name", "Tasks", "Open", "Collaborators", "Description"), rows);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleDetail() {
        try {
            long id = ConsoleUtils.readInt("Project ID: ");
            Project p = projectSvc.getById(id);

            ConsoleUtils.println("\n=== Project #" + p.getId() + " ===");
            ConsoleUtils.println("Name:        " + p.getName());
            ConsoleUtils.println("Description: " + nvl(p.getDescription()));

            List<Task> tasks = taskSvc.listAll().stream()
                .filter(t -> p.getId() == (t.getProjectId() != null ? t.getProjectId() : -1))
                .collect(Collectors.toList());
            long openCount      = tasks.stream().filter(t -> t.getStatus() == TaskStatus.OPEN).count();
            long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            long cancelledCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.CANCELLED).count();
            ConsoleUtils.println("\nTasks (" + tasks.size() + " total — "
                + openCount + " open, " + completedCount + " completed, " + cancelledCount + " cancelled):");
            if (tasks.isEmpty()) {
                ConsoleUtils.println("  (none)");
            } else {
                ConsoleUtils.printTable(
                    List.of("ID", "Title", "Priority", "Status", "Due Date"),
                    tasks.stream().map(t -> List.of(
                        String.valueOf(t.getId()),
                        t.getTitle(),
                        ConsoleUtils.colorPriority(t.getPriority().name()),
                        ConsoleUtils.colorStatus(t.getStatus().name()),
                        t.getDueDate() != null ? t.getDueDate().toString() : ""
                    )).collect(Collectors.toList())
                );
            }

            List<Collaborator> collaborators = collaboratorSvc.listByProject(id);
            ConsoleUtils.println("Collaborators (" + collaborators.size() + "):");
            if (collaborators.isEmpty()) {
                ConsoleUtils.println("  (none)");
            } else {
                ConsoleUtils.printTable(
                    List.of("ID", "Name", "Category", "Open/Limit"),
                    collaborators.stream().map(c -> {
                        int open = 0;
                        try { open = collaboratorSvc.countOpenTasks(c.getId()); } catch (TaskManagerException ignored) {}
                        int limit = c.getCategory().getOpenTaskLimit();
                        String load = open + "/" + limit + (open > limit ? " [OVERLOADED]" : "");
                        return List.of(String.valueOf(c.getId()), c.getName(), c.getCategory().name(), load);
                    }).collect(Collectors.toList())
                );
            }
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
