
import java.time.LocalDateTime;

public class ActivityEntry {
    private String description;
    private LocalDateTime timestamp;

    public ActivityEntry(){

    }

    public ActivityEntry(String desc, LocalDateTime timestamp){
        this();
        this.timestamp = timestamp;
        this.description = desc;
    }

    //SETTERS / GETTERS
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
