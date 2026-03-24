import java.util.ArrayList;
import java.util.List;

public class Project {
    private String title;       //mandatory and UNIQUE 
    private String description; //optional
    private List<Task> tasks;   //0..*
    private List<Collaborator> collaborators; //0..*

    public Project(){
        this.tasks = new ArrayList<>();
        this.collaborators = new ArrayList<>();
    }
    
    public Project(String title){
        this();
        this.title = title;
    }

    public Project(String title, String description){
        this();
        this.title = title;
        this.description = description;
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

    public List<Collaborator> getCollaborators() {
        return collaborators;
    }

    public void addCollaborator(Collaborator c) {
        this.collaborators.add(c);
    }

    public void removeCollaborator(Collaborator c) {
        this.collaborators.remove(c);
    }

    public Collaborator getCollaboratorByName(String name) {
        for (Collaborator c : collaborators) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null; // or throw an exception if not found
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
