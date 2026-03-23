import java.util.List;

public class Project {
    private int id;
    private String title;
    private String description; //optional
    private List<Task> tasks;

    public Project(){

    }

    public Project(String title){

    }


    public void addTask(Task t){
        this.tasks.add(t);
    }

    public void removeTask(Task t){
        this.tasks.remove(t);
    }
}
