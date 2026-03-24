
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CSVConnector {

    public static void exportTasks(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory\n");

            for (Task task : Database.getTasks()) {
                String subtasks = task.getSubtasks().stream()
                        .map(Subtask::getTitle)
                        .collect(Collectors.joining(";"));
                String collaborators = task.getProject() != null
                        ? task.getProject().getCollaborators().stream()
                            .map(c -> c.getName() + ":" + c.getCategory())
                            .collect(Collectors.joining(";"))
                        : "";

                // Prepare task line
                String[] line = {
                        escapeCSV(task.getTitle()),
                        escapeCSV(task.getDescription() != null ? task.getDescription() : ""),
                        escapeCSV(subtasks),
                        task.getStatus().toString(),  
                        task.getPriorityLevel().toString(),
                        task.getDueDate() != null ? task.getDueDate().toString() : "",
                        task.getProject() != null ? escapeCSV(task.getProject().getTitle()) : "",
                        task.getProject() != null ? escapeCSV(task.getProject().getDescription()) : "",
                        escapeCSV(collaborators)
                };

                // Write to CSV file
                writer.append(String.join(",", line)).append("\n");
            }

            System.out.println("Exported to CSV successfully: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to escape CSV commas and quotes
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }


    public static void importTasks(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header line

            while ((line = reader.readLine()) != null) {
                String[] values = parseCSVLine(line);

                Task task = new Task(values[0]);
                task.setDescription(values[1]);
                task.setStatus(Status.valueOf(values[3].trim()));  // Trim any extra spaces or escape codes
                task.setPriorityLevel(PriorityLevel.valueOf(values[4].trim()));
                if (!values[5].isEmpty()) task.setDueDate(LocalDate.parse(values[5]));

                // Project handling
                Project project = null;
                if (!values[6].isEmpty()) {
                    project = Database.getProjectByName(values[6]);
                    if (project == null) {
                        project = new Project(values[6], values[7]);
                        Database.addProject(project);
                    }
                    task.setProject(project);

                    // Collaborators
                    if (!values[8].isEmpty()) {
                        String[] collabs = values[8].split(";");
                        for (String c : collabs) {
                            String[] parts = c.split(":");
                            Collaborator col = project.getCollaboratorByName(parts[0]);
                            if (col == null) {
                                col = new Collaborator(parts[0], CollaboratorCategory.valueOf(parts[1]));
                                project.addCollaborator(col);
                            }
                        }
                    }
                }

                // Subtasks
                if (!values[2].isEmpty()) {
                    String[] subtasks = values[2].split(";");
                    for (String s : subtasks) task.addSubtask(new Subtask(s));
                }

                Database.addTask(task);
            }

            System.out.println("Imported tasks from CSV successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Simple CSV line parsing (handling quoted fields)
    private static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else sb.append(c);
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

}
