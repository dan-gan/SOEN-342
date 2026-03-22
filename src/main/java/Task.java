import java.time.LocalDateTime;

public class Task {
    private int id;
    private static int idCounter = 0; // Static counter to generate unique IDs for tasks
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime dueDate; 
    private PriorityLevel priorityLevel;
    private Status status;

    private Task(){
        this.creationDate = LocalDateTime.now();
        this.id = ++idCounter; // Assign a unique ID to each task
        this.priorityLevel = PriorityLevel.LOW;
        this.status = Status.OPEN;
    }

    public Task(String title){ //title necessary to create a task
        this();
        this.title = title;
    }

    public Task(String title, String desc, LocalDateTime dueDate){ //desc, dueDate optional
        this();
        this.description = desc;
        this.dueDate = dueDate;
    }

    //GETTERS
    public int getId() {return this.id;}
    public String getTitle() {return this.title;}
    public String getDescription() {return this.description;}
    public LocalDateTime getCreationDate() {return this.creationDate;}
    public LocalDateTime getDueDate() {return this.dueDate;}
    public PriorityLevel getPriorityLevel() {return this.priorityLevel;}
    public Status getStatus() {return this.status;}

    //SETTERS
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setDueDate(LocalDateTime dueDate) {this.dueDate = dueDate;}

    public void setHighPriority() {this.priorityLevel = PriorityLevel.HIGH;}
    public void setMediumPriority() {this.priorityLevel = PriorityLevel.MEDIUM;}
    public void setLowPriority() {this.priorityLevel = PriorityLevel.LOW;}

    public void completeTask() {this.status = Status.COMPLETED;}
    public void closeTask(){this.status = Status.CLOSED;}
    public void reopenTask(){this.status = Status.OPEN;}

    //equals
    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(o == null || this.getClass() != o.getClass())
            return false;

        Task t = (Task) o;
        return (this.id == t.id && this.title.equals(t.title))            
    }

    //toString
    @Override
    public String toString(){
        return "Task ID: " + this.id + "\nStatus: " + this.status + "\nPriority: " + this.priorityLevel 
        + "\nTask Title: " + this.title + "\nDescription" + this.description
        + "\ncreationDate: " + this.creationDate + "\ndueDate: " + this.dueDate; 
    }
    
    
    //other methods

}
