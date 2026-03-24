import java.util.ArrayList;
import java.util.List;

public class Database {
    private static List<Project> projects = new ArrayList<>();
    private static List<Task> tasks = new ArrayList<>();

    public static Project getProjectByName(String name) {
        return projects.stream().filter(p -> p.getTitle().equals(name)).findFirst().orElse(null);
    }
    public static void addProject(Project project) { projects.add(project); }

    public static List<Task> getTasks() { return tasks; }
    public static void addTask(Task task) { tasks.add(task); }
    public static void setTasks(List<Task> newTasks) { tasks = newTasks; }

    public static void reset() {
        projects.clear();
        tasks.clear();
    }
}
