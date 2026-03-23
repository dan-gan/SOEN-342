import java.util.List;

public class Project {
    private int id;
    private String title;       //mandatory
    private String description; //optional
    private List<Task> tasks;   //0..*

    public Project(){

    }

    public Project(String title){
        this();
        this.title = title;
    }

    //GETTERS / SETTERS
    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String desc){
        this.description = desc;
    }

    public List<Task> getTasks(){
        return this.tasks;
    }

    public void addTask(Task t){
        this.tasks.add(t);
    }

    public void removeTask(Task t){
        this.tasks.remove(t);
    }

    @Override
    public String toString(){
        String result = "Project Title: " + this.title + "\nDescription: " + this.description + "\n";
        for(Task t : tasks){
            result += t + "\n";
        }
        return result;
    }
}
