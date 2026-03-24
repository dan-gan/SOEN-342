import java.util.ArrayList;
import java.util.List;

public class Collaborator {
    private String name;
    private CollaboratorCategory category;
    private List<Task> tasks = new ArrayList<>();

    public Collaborator(String name, CollaboratorCategory category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CollaboratorCategory getCategory() { return category; }
    public void setCategory(CollaboratorCategory category) { this.category = category; }
    
    public List<Task> getTasks() { return tasks; }
    public void addTask(Task task) { tasks.add(task); }
}


