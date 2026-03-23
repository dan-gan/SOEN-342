public class Tag {
    private int id;
    private static int idCounter = 0;
    private String name;

    public Tag(){

    }

    public Tag(String name){
        this.name = name;
        this.id = ++idCounter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
