package com.taskmanager.cli;

import com.taskmanager.domain.model.Collaborator;
import com.taskmanager.domain.model.CollaboratorCategory;
import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.Subtask;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.service.CollaboratorService;
import com.taskmanager.domain.service.ProjectService;
import com.taskmanager.domain.service.SubtaskService;
import com.taskmanager.domain.service.TaskService;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.stream.Collectors;

public class CollaboratorMenu {

    private final CollaboratorService collaboratorSvc;
    private final ProjectService projectSvc;
    private final TaskService taskSvc;
    private final SubtaskService subtaskSvc;

    public CollaboratorMenu(CollaboratorService collaboratorSvc, ProjectService projectSvc,
                            TaskService taskSvc, SubtaskService subtaskSvc) {
        this.collaboratorSvc = collaboratorSvc;
        this.projectSvc = projectSvc;
        this.taskSvc = taskSvc;
        this.subtaskSvc = subtaskSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Collaborators ---");
            ConsoleUtils.println("1. Create collaborator");
            ConsoleUtils.println("2. List all collaborators");
            ConsoleUtils.println("3. View collaborator details");
            ConsoleUtils.println("4. Update collaborator");
            ConsoleUtils.println("5. Assign collaborator to task");
            ConsoleUtils.println("6. Set category limit");
            ConsoleUtils.println("7. Delete collaborator");
            ConsoleUtils.println("8. List overloaded collaborators");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 8);
            switch (choice) {
                case 1 -> handleCreate();
                case 2 -> handleListAll();
                case 3 -> handleDetail();
                case 4 -> handleUpdate();
                case 5 -> handleAssign();
                case 6 -> handleSetCategoryLimit();
                case 7 -> handleDelete();
                case 8 -> handleOverloaded();
                case 0 -> { return; }
            }
        }
    }

    private void handleListAll() {
        try {
            List<Collaborator> all = collaboratorSvc.listAll();
            if (all.isEmpty()) {
                ConsoleUtils.println("No collaborators found.");
                return;
            }
            java.util.Map<Long, String> projectNames = projectSvc.listAll().stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getName()));
            List<List<String>> rows = all.stream().map(c -> {
                int open, limit;
                try { open = collaboratorSvc.countOpenTasks(c.getId()); }
                catch (TaskManagerException e) { open = 0; }
                limit = c.getCategory().getOpenTaskLimit();
                String load = open + "/" + limit + (open > limit ? " [OVERLOADED]" : "");
                String project = projectNames.getOrDefault(c.getProjectId(), String.valueOf(c.getProjectId()));
                return List.of(String.valueOf(c.getId()), c.getName(), c.getCategory().name(), project, load);
            }).collect(Collectors.toList());
            ConsoleUtils.printTable(List.of("ID", "Name", "Category", "Project", "Open/Limit"), rows);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleDetail() {
        try {
            long id = ConsoleUtils.readInt("Collaborator ID: ");
            Collaborator c = collaboratorSvc.getById(id);
            int open  = collaboratorSvc.countOpenTasks(id);
            int limit = c.getCategory().getOpenTaskLimit();

            ConsoleUtils.println("\n=== Collaborator #" + c.getId() + " ===");
            ConsoleUtils.println("Name:     " + c.getName());
            ConsoleUtils.println("Category: " + c.getCategory() + "  (limit: " + limit + ")");
            ConsoleUtils.println("Load:     " + open + "/" + limit
                + (open > limit ? "  [OVERLOADED]" : open == limit ? "  [AT LIMIT]" : ""));

            // Project
            String projectDisplay = "(none)";
            if (c.getProjectId() != 0) {
                try {
                    Project p = projectSvc.getById(c.getProjectId());
                    projectDisplay = "[" + p.getId() + "] " + p.getName();
                } catch (TaskManagerException ignored) {
                    projectDisplay = String.valueOf(c.getProjectId());
                }
            }
            ConsoleUtils.println("Project:  " + projectDisplay);

            // Assigned tasks + subtask per task
            List<Long> taskIds = collaboratorSvc.getAssignedTaskIds(id);
            ConsoleUtils.println("\nAssigned Tasks (" + taskIds.size() + "):");
            if (taskIds.isEmpty()) {
                ConsoleUtils.println("  (none)");
            } else {
                ConsoleUtils.printTable(
                    List.of("Task ID", "Title", "Status", "Due Date", "Subtask", "Subtask Done"),
                    taskIds.stream().map(tid -> {
                        String title = "", status = "", due = "", subtaskTitle = "", subtaskDone = "";
                        try {
                            Task t = taskSvc.getById(tid);
                            title  = t.getTitle();
                            status = ConsoleUtils.colorStatus(t.getStatus().name());
                            due    = t.getDueDate() != null ? t.getDueDate().toString() : "";
                        } catch (TaskManagerException ignored) {}
                        try {
                            Long subtaskId = collaboratorSvc.getSubtaskIdForTask(id, tid);
                            if (subtaskId != null) {
                                Subtask s = subtaskSvc.getById(subtaskId);
                                subtaskTitle = s.getTitle();
                                subtaskDone  = s.isCompleted() ? "yes" : "no";
                            }
                        } catch (TaskManagerException ignored) {}
                        return List.of(String.valueOf(tid), title, status, due, subtaskTitle, subtaskDone);
                    }).collect(Collectors.toList())
                );
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleCreate() {
        try {
            String name = ConsoleUtils.readString("Collaborator name: ");
            CollaboratorCategory cat = readCategory();

            List<com.taskmanager.domain.model.Project> projects = projectSvc.listAll();
            if (projects.isEmpty()) {
                ConsoleUtils.printWarning("No projects exist. Create a project first.");
                return;
            }
            ConsoleUtils.printTable(List.of("ID", "Name"),
                projects.stream().map(p -> List.of(String.valueOf(p.getId()), p.getName()))
                    .collect(Collectors.toList()));
            long projectId = ConsoleUtils.readInt("Project ID: ");
            projectSvc.getById(projectId);

            Collaborator c = collaboratorSvc.addCollaboratorToProject(name, cat, projectId);
            ConsoleUtils.printSuccess("Collaborator created: [" + c.getId() + "] " + c.getName()
                + " (" + c.getCategory() + ") → project " + projectId);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleUpdate() {
        try {
            long id = ConsoleUtils.readInt("Collaborator ID: ");
            Collaborator c = collaboratorSvc.getById(id);
            ConsoleUtils.println("Current — Name: " + c.getName()
                + "  Category: " + c.getCategory()
                + "  Project ID: " + (c.getProjectId() != 0 ? c.getProjectId() : "(none)"));

            String name = ConsoleUtils.readOptionalString("New name (blank to keep): ");

            ConsoleUtils.println("New category (blank to keep current):");
            CollaboratorCategory[] cats = CollaboratorCategory.values();
            StringBuilder sb = new StringBuilder("  0=keep  ");
            for (int i = 0; i < cats.length; i++)
                sb.append((i + 1)).append("=").append(cats[i])
                  .append("(limit:").append(cats[i].getOpenTaskLimit()).append(")")
                  .append(i < cats.length - 1 ? "  " : "");
            ConsoleUtils.println(sb.toString());
            int catChoice = ConsoleUtils.readInt("Choose: ", 0, cats.length);
            CollaboratorCategory newCat = catChoice == 0 ? null : cats[catChoice - 1];

            List<com.taskmanager.domain.model.Project> projects = projectSvc.listAll();
            ConsoleUtils.printTable(List.of("ID", "Name"),
                projects.stream().map(p -> List.of(String.valueOf(p.getId()), p.getName()))
                    .collect(Collectors.toList()));
            String projectInput = ConsoleUtils.readOptionalString("New project ID (blank to keep): ");
            Long newProjectId = null;
            if (!projectInput.isBlank()) {
                newProjectId = Long.parseLong(projectInput);
                projectSvc.getById(newProjectId);
            }

            collaboratorSvc.updateCollaborator(id, name.isBlank() ? null : name, newCat, newProjectId);
            ConsoleUtils.printSuccess("Collaborator updated.");

            if (newCat != null) {
                var warnings = collaboratorSvc.detectOverloadedCollaborators();
                if (!warnings.isEmpty()) {
                    ConsoleUtils.printWarning("Overloaded collaborators after category change:");
                    warnings.forEach(w -> ConsoleUtils.println("  " + w));
                }
            }
        } catch (NumberFormatException e) {
            ConsoleUtils.printError("Project ID must be a valid number.");
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



    private void handleSetCategoryLimit() {
        try {
            CollaboratorCategory cat = readCategory();
            int current = collaboratorSvc.getCategoryLimit(cat);
            ConsoleUtils.println("Current limit for " + cat + ": " + current);
            int newLimit = ConsoleUtils.readInt("New limit (positive integer): ");
            collaboratorSvc.setCategoryLimit(cat, newLimit);
            ConsoleUtils.printSuccess("Limit for " + cat + " set to " + newLimit + ".");
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleOverloaded() {
        try {
            List<com.taskmanager.domain.service.CollaboratorOverloadWarning> warnings =
                collaboratorSvc.detectOverloadedCollaborators();
            if (warnings.isEmpty()) {
                ConsoleUtils.println("No overloaded collaborators.");
                return;
            }
            java.util.Map<Long, String> projectNames;
            try { projectNames = projectSvc.listAll().stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getName())); }
            catch (TaskManagerException e) { projectNames = java.util.Collections.emptyMap(); }
            final java.util.Map<Long, String> pNames = projectNames;

            ConsoleUtils.println("\n[OVERLOADED COLLABORATORS — " + warnings.size() + "]");
            for (com.taskmanager.domain.service.CollaboratorOverloadWarning w : warnings) {
                Collaborator c = w.getCollaborator();
                String project = c.getProjectId() != 0
                    ? pNames.getOrDefault(c.getProjectId(), String.valueOf(c.getProjectId()))
                    : "(none)";
                ConsoleUtils.println("\n  #" + c.getId() + "  " + c.getName()
                    + "  [" + c.getCategory() + "]"
                    + "  Load: " + w.getOpenTaskCount() + "/" + w.getLimit()
                    + "  Project: " + project);

                List<Long> taskIds = collaboratorSvc.getAssignedTaskIds(c.getId());
                if (taskIds.isEmpty()) {
                    ConsoleUtils.println("  No assigned tasks.");
                } else {
                    ConsoleUtils.printTable(
                        List.of("Task ID", "Title", "Status", "Due Date", "Subtask", "Done"),
                        taskIds.stream().map(tid -> {
                            String title = "", status = "", due = "", subtaskTitle = "", done = "";
                            try {
                                Task t = taskSvc.getById(tid);
                                title  = t.getTitle();
                                status = ConsoleUtils.colorStatus(t.getStatus().name());
                                due    = t.getDueDate() != null ? t.getDueDate().toString() : "";
                            } catch (TaskManagerException ignored) {}
                            try {
                                Long sid = collaboratorSvc.getSubtaskIdForTask(c.getId(), tid);
                                if (sid != null) {
                                    Subtask s = subtaskSvc.getById(sid);
                                    subtaskTitle = s.getTitle();
                                    done = s.isCompleted() ? "yes" : "no";
                                }
                            } catch (TaskManagerException ignored) {}
                            return List.of(String.valueOf(tid), title, status, due, subtaskTitle, done);
                        }).collect(Collectors.toList())
                    );
                }
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleDelete() {
        try {
            long id = ConsoleUtils.readInt("Collaborator ID: ");
            Collaborator c = collaboratorSvc.getById(id);
            if (ConsoleUtils.readYesNo("Delete collaborator \"" + c.getName() + "\"?")) {
                collaboratorSvc.deleteCollaborator(id);
                ConsoleUtils.printSuccess("Collaborator deleted.");
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private CollaboratorCategory readCategory() {
        CollaboratorCategory[] cats = CollaboratorCategory.values();
        StringBuilder sb = new StringBuilder("Category: ");
        for (int i = 0; i < cats.length; i++) {
            sb.append((i + 1)).append("=").append(cats[i])
              .append("(limit:").append(cats[i].getOpenTaskLimit()).append(")");
            if (i < cats.length - 1) sb.append("  ");
        }
        ConsoleUtils.println(sb.toString());
        int choice = ConsoleUtils.readInt("Choose: ", 1, cats.length);
        return cats[choice - 1];
    }
}
