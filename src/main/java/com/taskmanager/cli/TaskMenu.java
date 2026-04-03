package com.taskmanager.cli;

import com.taskmanager.domain.model.*;
import com.taskmanager.domain.service.*;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskMenu {

    private final TaskService taskSvc;
    private final SubtaskService subtaskSvc;
    private final TagService tagSvc;
    private final ProjectService projectSvc;
    private final ActivityService activitySvc;

    public TaskMenu(TaskService taskSvc, SubtaskService subtaskSvc, TagService tagSvc,
                    ProjectService projectSvc, ActivityService activitySvc) {
        this.taskSvc = taskSvc;
        this.subtaskSvc = subtaskSvc;
        this.tagSvc = tagSvc;
        this.projectSvc = projectSvc;
        this.activitySvc = activitySvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Tasks ---");
            ConsoleUtils.println("1. Create task");
            ConsoleUtils.println("2. List all tasks");
            ConsoleUtils.println("3. View task details");
            ConsoleUtils.println("4. Update task");
            ConsoleUtils.println("5. Change task status");
            ConsoleUtils.println("6. Manage subtasks");
            ConsoleUtils.println("7. View activity history");
            ConsoleUtils.println("8. Delete task");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 8);
            switch (choice) {
                case 1 -> handleCreate();
                case 2 -> handleList();
                case 3 -> handleView();
                case 4 -> handleUpdate();
                case 5 -> handleStatus();
                case 6 -> handleSubtasks();
                case 7 -> handleHistory();
                case 8 -> handleDelete();
                case 0 -> { return; }
            }
        }
    }

    private void handleCreate() {
        try {
            String title = ConsoleUtils.readString("Title: ");
            String desc = ConsoleUtils.readOptionalString("Description (optional): ");
            Priority priority = readPriority();
            Optional<LocalDate> dueDate = ConsoleUtils.readOptionalDate("Due date");
            Long projectId = readOptionalProjectId();
            Set<Long> tagIds = readTagIds();

            Task task = taskSvc.createTask(title, desc.isBlank() ? null : desc, priority,
                dueDate.orElse(null), projectId, tagIds);
            ConsoleUtils.printSuccess("Task created with ID: " + task.getId());
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleList() {
        try {
            List<Task> tasks = taskSvc.listAll();
            printTaskTable(tasks);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleView() {
        try {
            long id = ConsoleUtils.readInt("Task ID: ");
            Task task = taskSvc.getById(id);
            printTaskDetail(task);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleUpdate() {
        try {
            long id = ConsoleUtils.readInt("Task ID: ");
            taskSvc.getById(id); // validate exists
            ConsoleUtils.println("What to update?");
            ConsoleUtils.println("1. Title");
            ConsoleUtils.println("2. Description");
            ConsoleUtils.println("3. Priority");
            ConsoleUtils.println("4. Due date");
            ConsoleUtils.println("5. Project");
            ConsoleUtils.println("6. Tags");
            int choice = ConsoleUtils.readInt("Choose: ", 1, 6);
            switch (choice) {
                case 1 -> { taskSvc.updateTitle(id, ConsoleUtils.readString("New title: ")); ConsoleUtils.printSuccess("Updated."); }
                case 2 -> { taskSvc.updateDescription(id, ConsoleUtils.readOptionalString("New description: ")); ConsoleUtils.printSuccess("Updated."); }
                case 3 -> { taskSvc.updatePriority(id, readPriority()); ConsoleUtils.printSuccess("Updated."); }
                case 4 -> {
                    Optional<LocalDate> d = ConsoleUtils.readOptionalDate("New due date");
                    taskSvc.updateDueDate(id, d.orElse(null));
                    ConsoleUtils.printSuccess("Updated.");
                }
                case 5 -> { taskSvc.updateProject(id, readOptionalProjectId()); ConsoleUtils.printSuccess("Updated."); }
                case 6 -> { taskSvc.updateTags(id, readTagIds()); ConsoleUtils.printSuccess("Updated."); }
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleStatus() {
        try {
            long id = ConsoleUtils.readInt("Task ID: ");
            ConsoleUtils.println("1. Complete  2. Cancel  3. Reopen");
            int choice = ConsoleUtils.readInt("Choose: ", 1, 3);
            switch (choice) {
                case 1 -> { taskSvc.completeTask(id); ConsoleUtils.printSuccess("Task completed."); }
                case 2 -> { taskSvc.cancelTask(id); ConsoleUtils.printSuccess("Task cancelled."); }
                case 3 -> { taskSvc.reopenTask(id); ConsoleUtils.printSuccess("Task reopened."); }
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleSubtasks() {
        try {
            long taskId = ConsoleUtils.readInt("Task ID: ");
            taskSvc.getById(taskId); // validate
            while (true) {
                ConsoleUtils.println("\n-- Subtasks --");
                ConsoleUtils.println("1. Add subtask");
                ConsoleUtils.println("2. List subtasks");
                ConsoleUtils.println("3. Complete subtask");
                ConsoleUtils.println("4. Reopen subtask");
                ConsoleUtils.println("5. Delete subtask");
                ConsoleUtils.println("0. Back");
                int choice = ConsoleUtils.readInt("Choose: ", 0, 5);
                if (choice == 0) break;
                switch (choice) {
                    case 1 -> {
                        String t = ConsoleUtils.readString("Subtask title: ");
                        subtaskSvc.addSubtask(taskId, t);
                        ConsoleUtils.printSuccess("Subtask added.");
                    }
                    case 2 -> {
                        List<Subtask> subs = subtaskSvc.listByTask(taskId);
                        ConsoleUtils.printTable(List.of("ID", "Title", "Done"),
                            subs.stream().map(s -> List.of(
                                String.valueOf(s.getId()), s.getTitle(), s.isCompleted() ? "Yes" : "No"
                            )).collect(Collectors.toList()));
                    }
                    case 3 -> {
                        subtaskSvc.completeSubtask(ConsoleUtils.readInt("Subtask ID: "));
                        ConsoleUtils.printSuccess("Subtask completed.");
                    }
                    case 4 -> {
                        subtaskSvc.reopenSubtask(ConsoleUtils.readInt("Subtask ID: "));
                        ConsoleUtils.printSuccess("Subtask reopened.");
                    }
                    case 5 -> {
                        subtaskSvc.deleteSubtask(ConsoleUtils.readInt("Subtask ID: "));
                        ConsoleUtils.printSuccess("Subtask deleted.");
                    }
                }
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleHistory() {
        try {
            long id = ConsoleUtils.readInt("Task ID: ");
            List<ActivityEntry> history = activitySvc.getHistory(id);
            ConsoleUtils.printTable(List.of("Timestamp", "Description"),
                history.stream().map(e -> List.of(e.getTimestamp().toString(), e.getDescription()))
                    .collect(Collectors.toList()));
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleDelete() {
        try {
            long id = ConsoleUtils.readInt("Task ID to delete: ");
            if (ConsoleUtils.readYesNo("Are you sure?")) {
                taskSvc.deleteTask(id);
                ConsoleUtils.printSuccess("Task deleted.");
            }
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    void printTaskTable(List<Task> tasks) {
        ConsoleUtils.printTable(List.of("ID", "Title", "Priority", "Status", "Due Date", "Project"),
            tasks.stream().map(t -> List.of(
                String.valueOf(t.getId()), t.getTitle(), t.getPriority().name(), t.getStatus().name(),
                t.getDueDate() != null ? t.getDueDate().toString() : "",
                t.getProjectId() != null ? String.valueOf(t.getProjectId()) : ""
            )).collect(Collectors.toList()));
    }

    private void printTaskDetail(Task task) throws TaskManagerException {
        ConsoleUtils.println("\n=== Task #" + task.getId() + " ===");
        ConsoleUtils.println("Title:       " + task.getTitle());
        ConsoleUtils.println("Description: " + nvl(task.getDescription()));
        ConsoleUtils.println("Created:     " + task.getCreationDate());
        ConsoleUtils.println("Priority:    " + task.getPriority());
        ConsoleUtils.println("Status:      " + task.getStatus());
        ConsoleUtils.println("Due Date:    " + (task.getDueDate() != null ? task.getDueDate() : "none"));
        ConsoleUtils.println("Project ID:  " + (task.getProjectId() != null ? task.getProjectId() : "none"));
        String tags = task.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "));
        ConsoleUtils.println("Tags:        " + (tags.isEmpty() ? "none" : tags));

        List<Subtask> subtasks = subtaskSvc.listByTask(task.getId());
        if (!subtasks.isEmpty()) {
            ConsoleUtils.println("Subtasks (" + subtasks.stream().filter(Subtask::isCompleted).count() +
                "/" + subtasks.size() + " done):");
            for (Subtask s : subtasks) {
                ConsoleUtils.println("  [" + (s.isCompleted() ? "x" : " ") + "] " + s.getTitle());
            }
        }
    }

    private Priority readPriority() {
        ConsoleUtils.println("Priority: 1=LOW  2=MEDIUM  3=HIGH");
        int choice = ConsoleUtils.readInt("Choose: ", 1, 3);
        return switch (choice) { case 1 -> Priority.LOW; case 3 -> Priority.HIGH; default -> Priority.MEDIUM; };
    }

    private Long readOptionalProjectId() throws TaskManagerException {
        String input = ConsoleUtils.readOptionalString("Project ID (blank for none): ");
        if (input.isBlank()) return null;
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new ValidationException("Project ID must be a valid number.");
        }
    }

    private Set<Long> readTagIds() throws TaskManagerException {
        String input = ConsoleUtils.readOptionalString("Tags (comma-separated names, blank for none): ");
        if (input.isBlank()) return Collections.emptySet();
        Set<Long> ids = new HashSet<>();
        for (String name : input.split(",")) {
            ids.add(tagSvc.findOrCreate(name.trim()).getId());
        }
        return ids;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
