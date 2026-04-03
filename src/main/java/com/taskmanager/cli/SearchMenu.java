package com.taskmanager.cli;

import com.taskmanager.domain.gateway.ICalGateway;
import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.Tag;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.service.*;
import com.taskmanager.exception.TaskManagerException;

import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchMenu {

    private final SearchService searchSvc;
    private final ProjectService projectSvc;
    private final TagService tagSvc;
    private final CsvService csvSvc;
    private final ICalGateway icalGateway;
    private final SubtaskService subtaskSvc;

    public SearchMenu(SearchService searchSvc, ProjectService projectSvc, TagService tagSvc,
                      CsvService csvSvc, ICalGateway icalGateway, SubtaskService subtaskSvc) {
        this.searchSvc = searchSvc;
        this.projectSvc = projectSvc;
        this.tagSvc = tagSvc;
        this.csvSvc = csvSvc;
        this.icalGateway = icalGateway;
        this.subtaskSvc = subtaskSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Search Tasks ---");
            ConsoleUtils.println("1. Search with criteria");
            ConsoleUtils.println("2. List all open tasks (default)");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 2);
            switch (choice) {
                case 1 -> handleSearch();
                case 2 -> handleDefault();
                case 0 -> { return; }
            }
        }
    }

    private void handleSearch() {
        try {
            SearchCriteria criteria = new SearchCriteria();
            ConsoleUtils.println("Enter search criteria (blank/0 to skip each):");

            String keyword = ConsoleUtils.readOptionalString("Keyword (title/description match): ");
            if (!keyword.isBlank()) criteria.setKeyword(keyword);

            ConsoleUtils.println("Status: 0=any  1=OPEN  2=COMPLETED  3=CANCELLED");
            int statusChoice = ConsoleUtils.readInt("Choose: ", 0, 3);
            if (statusChoice == 1) criteria.setStatus(TaskStatus.OPEN);
            else if (statusChoice == 2) criteria.setStatus(TaskStatus.COMPLETED);
            else if (statusChoice == 3) criteria.setStatus(TaskStatus.CANCELLED);

            ConsoleUtils.println("Priority: 0=any  1=LOW  2=MEDIUM  3=HIGH");
            int prioChoice = ConsoleUtils.readInt("Choose: ", 0, 3);
            if (prioChoice == 1) criteria.setPriority(Priority.LOW);
            else if (prioChoice == 2) criteria.setPriority(Priority.MEDIUM);
            else if (prioChoice == 3) criteria.setPriority(Priority.HIGH);

            Optional<LocalDate> dueDate = ConsoleUtils.readOptionalDate("Exact due date");
            dueDate.ifPresent(criteria::setDueDate);

            Optional<LocalDate> rangeStart = ConsoleUtils.readOptionalDate("Due date range start");
            rangeStart.ifPresent(criteria::setDateRangeStart);

            Optional<LocalDate> rangeEnd = ConsoleUtils.readOptionalDate("Due date range end");
            rangeEnd.ifPresent(criteria::setDateRangeEnd);

            ConsoleUtils.println("Day of week: 0=any  1=MON  2=TUE  3=WED  4=THU  5=FRI  6=SAT  7=SUN");
            int dowChoice = ConsoleUtils.readInt("Choose: ", 0, 7);
            if (dowChoice > 0) criteria.setDayOfWeek(DayOfWeek.of(dowChoice));

            List<com.taskmanager.domain.model.Project> allProjects = projectSvc.listAll();
            String projectName = ConsoleUtils.readOptionalString("Project name (blank to skip): ");
            if (!projectName.isBlank()) {
                allProjects.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(projectName))
                    .findFirst()
                    .ifPresentOrElse(
                        p -> criteria.setProjectId(p.getId()),
                        () -> {
                            ConsoleUtils.printWarning("No project found with name \"" + projectName + "\". Skipping.");
                            String names = allProjects.stream()
                                .map(com.taskmanager.domain.model.Project::getName)
                                .collect(Collectors.joining(", "));
                            ConsoleUtils.println("Available: " + (names.isEmpty() ? "(none)" : names));
                        }
                    );
            }

            String tagName = ConsoleUtils.readOptionalString("Tag name (blank to skip): ");
            if (!tagName.isBlank()) {
                tagSvc.listAll().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(tagName))
                    .findFirst()
                    .ifPresent(t -> criteria.setTagId(t.getId()));
            }

            List<Task> results = searchSvc.search(criteria);
            ConsoleUtils.println("Found " + results.size() + " task(s).");
            printTaskTable(results);
            offerExport(results);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void handleDefault() {
        try {
            List<Task> results = searchSvc.search(null);
            ConsoleUtils.println("Open tasks (" + results.size() + "):");
            printTaskTable(results);
            offerExport(results);
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void offerExport(List<Task> results) {
        if (results.isEmpty()) return;
        ConsoleUtils.println("Export results? 0=No  1=CSV  2=iCal");
        int choice = ConsoleUtils.readInt("Choose: ", 0, 2);
        if (choice == 0) return;
        String path = ConsoleUtils.readString("Output file path: ");
        try {
            if (choice == 1) {
                try (FileOutputStream fos = new FileOutputStream(path)) {
                    csvSvc.exportTasks(results, fos);
                    ConsoleUtils.printSuccess("Exported " + results.size() + " tasks to " + path);
                }
            } else {
                try (FileOutputStream fos = new FileOutputStream(path)) {
                    icalGateway.exportTasks(
                        results,
                        projectId -> { try { return projectSvc.getById(projectId); } catch (Exception e) { return null; } },
                        taskId -> { try { return subtaskSvc.listByTask(taskId).stream()
                            .map(s -> (s.isCompleted() ? "[x] " : "[ ] ") + s.getTitle())
                            .collect(Collectors.toList()); } catch (Exception e) { return List.of(); } },
                        fos
                    );
                    ConsoleUtils.printSuccess("Exported to " + path);
                }
            }
        } catch (Exception e) {
            ConsoleUtils.printError("Export failed: " + e.getMessage());
        }
    }

    private void printTaskTable(List<Task> tasks) {
        ConsoleUtils.printTable(
            List.of("ID", "Title", "Priority", "Status", "Due Date", "Project ID"),
            tasks.stream().map(t -> List.of(
                String.valueOf(t.getId()), t.getTitle(), t.getPriority().name(), t.getStatus().name(),
                t.getDueDate() != null ? t.getDueDate().toString() : "",
                t.getProjectId() != null ? String.valueOf(t.getProjectId()) : ""
            )).collect(java.util.stream.Collectors.toList())
        );
    }
}
