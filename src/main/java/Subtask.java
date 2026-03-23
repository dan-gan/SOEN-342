public class Subtask{
    private int id;
    private static int idCounter = 0;
    private String title;
    private Status status;

    public Subtask(){

    }

    public Subtask(String title, Status status){
        this();
        this.id = ++idCounter;
        this.title = title;
        this.status = status;
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