public class Subtask{
    private int id;
    private static int idCounter = 0;
    private String title;
    private Status status;

    public Subtask(){
        this.status = Status.OPEN;
    }

    public Subtask(String title){
        this();
        this.id = ++idCounter;
        this.title = title;
    }

    //SETTERS / GETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


}