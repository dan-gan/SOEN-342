package com.taskmanager.domain.service;

import com.taskmanager.domain.model.*;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.repository.CollaboratorRepository;
import com.taskmanager.persistence.repository.SubtaskRepository;
import com.taskmanager.persistence.repository.TagRepository;
import com.taskmanager.persistence.repository.TaskRepository;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class CsvService {

    private static final String[] HEADERS = {
        "TaskName", "Description", "Subtask", "Status", "Priority",
        "DueDate", "ProjectName", "ProjectDescription", "Collaborator", "CollaboratorCategory"
    };

    private final TaskService taskSvc;
    private final ProjectService projectSvc;
    private final CollaboratorService collaboratorSvc;
    private final SubtaskService subtaskSvc;
    private final TagService tagSvc;
    private final TaskRepository taskRepo;
    private final CollaboratorRepository collaboratorRepo;
    private final SubtaskRepository subtaskRepo;
    private final TagRepository tagRepo;

    public CsvService(TaskService taskSvc, ProjectService projectSvc,
                      CollaboratorService collaboratorSvc, SubtaskService subtaskSvc,
                      TagService tagSvc, TaskRepository taskRepo,
                      CollaboratorRepository collaboratorRepo, SubtaskRepository subtaskRepo,
                      TagRepository tagRepo) {
        this.taskSvc = taskSvc;
        this.projectSvc = projectSvc;
        this.collaboratorSvc = collaboratorSvc;
        this.subtaskSvc = subtaskSvc;
        this.tagSvc = tagSvc;
        this.taskRepo = taskRepo;
        this.collaboratorRepo = collaboratorRepo;
        this.subtaskRepo = subtaskRepo;
        this.tagRepo = tagRepo;
    }

    public void exportTasks(List<Task> tasks, OutputStream out) throws TaskManagerException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"))) {
            writer.println(String.join(",", Arrays.stream(HEADERS).map(this::csvEscape).toArray(String[]::new)));

            for (Task task : tasks) {
                String projectName = "";
                String projectDescription = "";
                if (task.getProjectId() != null) {
                    Optional<Project> proj = projectSvc.listAll().stream()
                        .filter(p -> p.getId() == task.getProjectId()).findFirst();
                    if (proj.isPresent()) {
                        projectName = proj.get().getName();
                        projectDescription = nvl(proj.get().getDescription());
                    }
                }

                List<Subtask> subtasks = subtaskSvc.listByTask(task.getId());
                List<Long> collaboratorIds = collaboratorRepo.findCollaboratorIdsByTaskId(task.getId());

                if (subtasks.isEmpty() && collaboratorIds.isEmpty()) {
                    writer.println(buildRow(task, "", projectName, projectDescription, "", ""));
                } else {
                    // Write one row per subtask
                    boolean wroteRow = false;
                    for (Subtask st : subtasks) {
                        // Check if this subtask belongs to a collaborator assignment
                        String collabName = "";
                        String collabCategory = "";
                        for (long collabId : collaboratorIds) {
                            Collaborator c = collaboratorSvc.getById(collabId);
                            // The collaborator subtask has title = collaborator name
                            if (st.getTitle().equals(c.getName())) {
                                collabName = c.getName();
                                collabCategory = c.getCategory().name();
                                break;
                            }
                        }
                        writer.println(buildRow(task, st.getTitle(), projectName, projectDescription,
                            collabName, collabCategory));
                        wroteRow = true;
                    }
                    // If there are collaborator assignments without matching subtasks listed
                    if (!wroteRow) {
                        writer.println(buildRow(task, "", projectName, projectDescription, "", ""));
                    }
                }
            }
        } catch (IOException e) {
            throw new TaskManagerException("Failed to export CSV", e);
        }
    }

    public ImportResult importTasks(InputStream in) throws TaskManagerException {
        int successCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return new ImportResult(0, List.of("Empty file."));

            // Map column indices
            String[] cols = parseCsvLine(headerLine);
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < cols.length; i++) colIndex.put(cols[i].trim(), i);

            // Group rows by TaskName
            Map<String, List<String[]>> grouped = new LinkedHashMap<>();
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                String[] row = parseCsvLine(line);
                String taskName = get(row, colIndex, "TaskName");
                if (taskName == null || taskName.isBlank()) {
                    errors.add("Line " + lineNum + ": missing TaskName.");
                    continue;
                }
                grouped.computeIfAbsent(taskName, k -> new ArrayList<>()).add(row);
            }

            for (Map.Entry<String, List<String[]>> entry : grouped.entrySet()) {
                String taskName = entry.getKey();
                List<String[]> rows = entry.getValue();
                try {
                    importTaskGroup(taskName, rows, colIndex);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Task '" + taskName + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new TaskManagerException("Failed to read CSV file", e);
        }
        return new ImportResult(successCount, errors);
    }

    private void importTaskGroup(String taskName, List<String[]> rows, Map<String, Integer> colIndex)
            throws TaskManagerException {
        String[] firstRow = rows.get(0);
        String description = get(firstRow, colIndex, "Description");
        String statusStr = get(firstRow, colIndex, "Status");
        String priorityStr = get(firstRow, colIndex, "Priority");
        String dueDateStr = get(firstRow, colIndex, "DueDate");
        String projectName = get(firstRow, colIndex, "ProjectName");
        String projectDescription = get(firstRow, colIndex, "ProjectDescription");

        Priority priority = (priorityStr != null && !priorityStr.isBlank())
            ? Priority.fromString(priorityStr) : Priority.MEDIUM;
        LocalDate dueDate = (dueDateStr != null && !dueDateStr.isBlank())
            ? LocalDate.parse(dueDateStr) : null;

        Long projectId = null;
        if (projectName != null && !projectName.isBlank()) {
            Optional<Project> existing = projectSvc.findByName(projectName);
            if (existing.isPresent()) {
                projectId = existing.get().getId();
            } else {
                Project proj = projectSvc.createProject(projectName, projectDescription);
                projectId = proj.getId();
            }
        }

        String dueDateStr2 = dueDate != null ? dueDate.toString() : null;
        if (dueDateStr2 != null && taskRepo.existsByTitleAndDueDate(taskName, dueDateStr2)) {
            throw new TaskManagerException("Duplicate: task '" + taskName + "' with due date " + dueDateStr2 + " already exists.");
        }

        Task task = taskSvc.createTask(taskName, description, priority, dueDate, projectId, null);

        // Apply status if not OPEN
        if (statusStr != null && !statusStr.isBlank()) {
            TaskStatus status = TaskStatus.fromString(statusStr);
            if (status == TaskStatus.COMPLETED) taskSvc.completeTask(task.getId());
            else if (status == TaskStatus.CANCELLED) taskSvc.cancelTask(task.getId());
        }

        // Process subtask/collaborator rows
        for (String[] row : rows) {
            String subtaskTitle = get(row, colIndex, "Subtask");
            String collabName = get(row, colIndex, "Collaborator");
            String collabCatStr = get(row, colIndex, "CollaboratorCategory");

            if (collabName != null && !collabName.isBlank() && projectId != null) {
                CollaboratorCategory cat = (collabCatStr != null && !collabCatStr.isBlank())
                    ? CollaboratorCategory.fromString(collabCatStr) : CollaboratorCategory.JUNIOR;
                try {
                    Collaborator collab = collaboratorSvc.findOrCreate(collabName, cat, projectId);
                    collaboratorSvc.assignCollaboratorToTask(collab.getId(), task.getId());
                } catch (Exception e) {
                    // Log but don't fail the task import
                }
            } else if (subtaskTitle != null && !subtaskTitle.isBlank()) {
                try {
                    subtaskSvc.addSubtask(task.getId(), subtaskTitle);
                } catch (Exception e) {
                    // Log but don't fail the task import
                }
            }
        }
    }

    private String buildRow(Task task, String subtaskTitle, String projectName,
                            String projectDescription, String collabName, String collabCategory) {
        return String.join(",",
            csvEscape(task.getTitle()),
            csvEscape(nvl(task.getDescription())),
            csvEscape(subtaskTitle),
            csvEscape(task.getStatus().name()),
            csvEscape(task.getPriority().name()),
            csvEscape(task.getDueDate() != null ? task.getDueDate().toString() : ""),
            csvEscape(projectName),
            csvEscape(projectDescription),
            csvEscape(collabName),
            csvEscape(collabCategory)
        );
    }

    private String csvEscape(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return "\"" + value + "\"";
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    sb.append(c);
                }
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private String get(String[] row, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx == null || idx >= row.length) return null;
        String val = row[idx].trim();
        return val.isEmpty() ? null : val;
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    public static class ImportResult {
        private final int successCount;
        private final List<String> errors;

        public ImportResult(int successCount, List<String> errors) {
            this.successCount = successCount;
            this.errors = errors;
        }

        public int getSuccessCount() { return successCount; }
        public List<String> getErrors() { return errors; }
    }
}
