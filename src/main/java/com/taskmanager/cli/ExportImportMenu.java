package com.taskmanager.cli;

import com.taskmanager.domain.gateway.ICalGateway;
import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.service.*;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.repository.SubtaskRepository;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExportImportMenu {

    private final CsvService csvSvc;
    private final ICalGateway icalGateway;
    private final SearchService searchSvc;
    private final ProjectService projectSvc;
    private final TaskService taskSvc;
    private final SubtaskService subtaskSvc;

    public ExportImportMenu(CsvService csvSvc, ICalGateway icalGateway,
                            SearchService searchSvc, ProjectService projectSvc,
                            TaskService taskSvc, SubtaskService subtaskSvc) {
        this.csvSvc = csvSvc;
        this.icalGateway = icalGateway;
        this.searchSvc = searchSvc;
        this.projectSvc = projectSvc;
        this.taskSvc = taskSvc;
        this.subtaskSvc = subtaskSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Export / Import ---");
            ConsoleUtils.println("1. Export all tasks to CSV");
            ConsoleUtils.println("2. Import tasks from CSV");
            ConsoleUtils.println("3. Export all tasks to iCal (.ics)");
            ConsoleUtils.println("4. Export project tasks to iCal (.ics)");
            ConsoleUtils.println("5. Export open tasks to iCal (.ics)");
            ConsoleUtils.println("6. Export single task to iCal (.ics)");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 6);
            switch (choice) {
                case 1 -> handleCsvExport();
                case 2 -> handleCsvImport();
                case 3 -> handleIcalExportAll();
                case 4 -> handleIcalExportProject();
                case 5 -> handleIcalExportOpen();
                case 6 -> handleIcalExportSingle();
                case 0 -> { return; }
            }
        }
    }

    private void handleCsvExport() {
        String path = ConsoleUtils.readString("Output file path (e.g. tasks.csv): ");
        try (FileOutputStream fos = new FileOutputStream(path)) {
            List<Task> tasks = taskSvc.listAll();
            csvSvc.exportTasks(tasks, fos);
            ConsoleUtils.printSuccess("Exported " + tasks.size() + " tasks to " + path);
        } catch (Exception e) {
            ConsoleUtils.printError("Export failed: " + e.getMessage());
        }
    }

    private void handleCsvImport() {
        String path = ConsoleUtils.readString("Input file path (e.g. tasks.csv): ");
        try (FileInputStream fis = new FileInputStream(path)) {
            CsvService.ImportResult result = csvSvc.importTasks(fis);
            ConsoleUtils.printSuccess("Imported " + result.getSuccessCount() + " tasks.");
            if (!result.getErrors().isEmpty()) {
                ConsoleUtils.printWarning("Errors:");
                result.getErrors().forEach(e -> ConsoleUtils.println("  " + e));
            }
        } catch (Exception e) {
            ConsoleUtils.printError("Import failed: " + e.getMessage());
        }
    }

    private void handleIcalExportAll() {
        String path = ConsoleUtils.readString("Output file path (e.g. tasks.ics): ");
        try (FileOutputStream fos = new FileOutputStream(path)) {
            List<Task> tasks = taskSvc.listAll();
            exportIcal(tasks, path, fos);
        } catch (Exception e) {
            ConsoleUtils.printError("Export failed: " + e.getMessage());
        }
    }

    private void handleIcalExportProject() {
        try {
            long projectId = ConsoleUtils.readInt("Project ID: ");
            Project project = projectSvc.getById(projectId);
            String path = ConsoleUtils.readString("Output file path (e.g. project.ics): ");
            List<Task> tasks = taskSvc.listAll().stream()
                .filter(t -> project.getId() == (t.getProjectId() != null ? t.getProjectId() : -1))
                .collect(Collectors.toList());
            try (FileOutputStream fos = new FileOutputStream(path)) {
                exportIcal(tasks, path, fos);
            }
        } catch (Exception e) {
            ConsoleUtils.printError("Export failed: " + e.getMessage());
        }
    }

    private void handleIcalExportSingle() {
        try {
            long taskId = ConsoleUtils.readInt("Task ID: ");
            Task task = taskSvc.getById(taskId);
            if (task.getDueDate() == null) {
                ConsoleUtils.printError("Task has no due date and cannot be exported to iCal.");
                return;
            }
            String path = ConsoleUtils.readString("Output file path (e.g. task.ics): ");
            try (FileOutputStream fos = new FileOutputStream(path)) {
                exportIcal(List.of(task), path, fos);
            }
        } catch (Exception e) {
            ConsoleUtils.printError("Export failed: " + e.getMessage());
        }
    }

    private void handleIcalExportOpen() {
        String path = ConsoleUtils.readString("Output file path (e.g. open.ics): ");
        try (FileOutputStream fos = new FileOutputStream(path)) {
            List<Task> tasks = searchSvc.search(null); // default = all open tasks
            exportIcal(tasks, path, fos);
        } catch (Exception e) {
            ConsoleUtils.printError("Export failed: " + e.getMessage());
        }
    }

    private void exportIcal(List<Task> tasks, String path, OutputStream out) throws TaskManagerException {
        icalGateway.exportTasks(
            tasks,
            projectId -> {
                try { return projectSvc.getById(projectId); } catch (Exception e) { return null; }
            },
            taskId -> {
                try {
                    return subtaskSvc.listByTask(taskId).stream()
                        .map(s -> (s.isCompleted() ? "[x] " : "[ ] ") + s.getTitle())
                        .collect(Collectors.toList());
                } catch (Exception e) { return List.of(); }
            },
            out
        );
        ConsoleUtils.printSuccess("Exported to " + path);
    }
}
