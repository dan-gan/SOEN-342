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


    public Task(){             //USED ONLY IN CSV IMPORT 
        this.creationDate = LocalDate.now();
        this.priorityLevel = PriorityLevel.LOW;
        this.status = Status.OPEN;

        this.subtasks = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.activityEntries = new ArrayList<>();
    }

    public Task(String title){ //ONLY for user (title necessary to create a task)
        this();
        this.id = ++idCounter; 
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
    void setId(int id){this.id = id;} //ONLY for CSVConnector
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setCreationDate(LocalDate creationDatet) {this.creationDate = creationDatet;}
    public void setDueDate(LocalDate dueDate) {this.dueDate = dueDate;}
    public void setPriorityLevel(PriorityLevel pl) {this.priorityLevel = pl;}
    public void setStatus(Status status) {this.status = status;}
    public void setProject(Project p){this.project = p;}

    public void setSubtasks(List<Subtask> subtasks){this.subtasks = subtasks;}
    public void setTags(List<Tag> tags){this.tags = tags;}
    public void setActivityEntries(List<ActivityEntry> activityEntries){this.activityEntries = activityEntries;}


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
        return "\nTask ID: " + this.id 
        + "\nTask Title: " + this.title 
        + "\nStatus: " + this.status.colored() 
        + "\nPriority: " + this.priorityLevel.colored() 
        + "\nDescription: " + this.description 
        + "\ncreationDate: " + this.creationDate + " " + this.creationDate.getDayOfWeek() 
        + "\ndueDate: " + this.dueDate + " " + (this.dueDate != null ? this.dueDate.getDayOfWeek() : "")
        + (this.project != null ? "\n" + this.project.toString() : "")
        // + "\nSubtasks: " + this.subtasks.toString()
        // + "\nTags: " + this.tags.toString()
        // + "\nActivity Entries: " + this.activityEntries.toString() 
        + "\n-----------------------------";
    } 
    
    
    //other methods

}
