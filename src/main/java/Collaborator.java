import java.util.ArrayList;
import java.util.List;

public class Collaborator {
    private String name;
    private CollaboratorCategory category;
    private List<Task> tasks = new ArrayList<>();
    public static int limitForJuniors = 10;
    public static int limitForIntermediate = 5;
    public static int limitForSeniors = 2;

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

    public int getOpenTaskCount(){
        int count = 0;
        for(Task task: tasks){
            if(task.getStatus() == Status.OPEN){
                count++;
            }
        }
        return count;
    }

    public boolean isOverloaded(){
        int countOpenAssignedTasks = getOpenTaskCount();
        if(category == CollaboratorCategory.JUNIOR && countOpenAssignedTasks > limitForJuniors){
            return true;
        }
        else if(category == CollaboratorCategory.INTERMEDIATE && countOpenAssignedTasks > limitForIntermediate){
            return true;
        }
        else if(category == CollaboratorCategory.SENIOR && countOpenAssignedTasks > limitForSeniors){
            return true;
        }
        else{
            return false;
        }
    }

    public int getLimit(){
        if(category == CollaboratorCategory.JUNIOR){
            return limitForJuniors;
        }
        else if(category == CollaboratorCategory.INTERMEDIATE){
            return limitForIntermediate;
        }
        else if(category == CollaboratorCategory.SENIOR){
            return limitForSeniors;
        }
        else{
            return 0;
        }
    }
}


