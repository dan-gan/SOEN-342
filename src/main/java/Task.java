import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Task {
    private int id;
    private static int idCounter = 0; // Static counter to generate unique IDs for tasks
    private String title;
    private String description;       //optional
    private LocalDate creationDate;
    private LocalDate dueDate; 
    private PriorityLevel priorityLevel;
    private Status status;

    private Project project;

    private List<Subtask> subtasks;
    private List<Tag> tags;
    private List<ActivityEntry> activityEntries;


    private Task(){ //private default constructor, called by parametrized constructors
        this.creationDate = LocalDate.now();
        this.id = ++idCounter; 
        this.priorityLevel = PriorityLevel.LOW;
        this.status = Status.OPEN;
        this.subtasks = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.activityEntries = new ArrayList<>();
    }

    public Task(String title){ //title necessary to create a task
        this();
        this.title = title;
    }

    //GETTERS
    public int getId() {return this.id;}
    public String getTitle() {return this.title;}
    public String getDescription() {return this.description;}
    public LocalDate getCreationDate() {return this.creationDate;}
    public LocalDate getDueDate() {return this.dueDate;}
    public PriorityLevel getPriorityLevel() {return this.priorityLevel;}
    public Status getStatus() {return this.status;}
    public Project getProject() {return this.project;}

    public List<Subtask> getSubtasks() {return this.subtasks;}
    public List<Tag> getTags() {return this.tags;}
    public List<ActivityEntry> getActivityEntries() {return this.activityEntries;}
    

    //SETTERS
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setCreationDate(LocalDate creationDatet) {this.creationDate = creationDatet;}
    public void setDueDate(LocalDate dueDate) {this.dueDate = dueDate;}
    public void setPriority(PriorityLevel pl) {this.priorityLevel = pl;}
    public void setStatus(Status status) {this.status = status;}
    public void setProject(Project p){this.project = p;}

    public void addSubtask(Subtask subtask){
        this.subtasks.add(subtask);
    }
    public void removeSubtask(Subtask subtask){
        this.subtasks.remove(subtask);
    }

    public void addTag(Tag tag){
        this.tags.add(tag);
    }
    public void removeTag(Tag tag){
        this.tags.remove(tag);
    }

    public void addActivityEntry(ActivityEntry ae){
        this.activityEntries.add(ae);
    }
    public void removeActivityEntry(ActivityEntry ae){
        this.activityEntries.remove(ae);
    }

    //equals
    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(o == null || this.getClass() != o.getClass())
            return false;

        Task t = (Task) o;
        return (this.id == t.id);            
    }

    //toString
    @Override
    public String toString(){
        return "Task ID: " + this.id + "\nStatus: " + this.status + "\nPriority: " + this.priorityLevel 
        + "\nTask Title: " + this.title + "\nDescription: " + this.description
        + "\ncreationDate: " + this.creationDate + "\ndueDate: " + this.dueDate; 
    }
    
    
    //other methods

}
