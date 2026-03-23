import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;

public class TaskManager {


    private static List<Task> tasks = new ArrayList<>();

    private static CSVConnector CSVConnector = new CSVConnector();

    public static void main(String[] args) {
        
        Scanner kb = new Scanner(System.in);     

        System.out.println("\n\nWelcome to your Personal Task Management System!");

        int input = -1;
        while(input != 0){
            System.out.println("\nChoose an option: ");
            System.out.println("\t1. Create a Task");
            System.out.println("\t2. Search Tasks");
            // System.out.println("\t3. Update a Task");
            // System.out.println("\t4. Complete a Task");
            // System.out.println("\t5. Cancel a Task");
            System.out.println("\t6. Export Tasks to CSV");
            System.out.println("\t7. Import Tasks from CSV");
            // System.out.println("\t8. Create a Project");
            System.out.println("\t0. Exit");
            input = kb.nextInt();
            kb.nextLine();
            switch(input){
                case 0:
                    System.out.println("Thank you for using PTMS!");
                    System.exit(0);
                    break;
                case 1:
                    System.out.println("Enter a task title: ");
                    String title = kb.nextLine();
                    System.out.println(createTask(title));
                    break;
                case 2:
                    int input2 = -1;
                    while(input2 != 0){
                        System.out.println("Choose an option: ");
                        System.out.println("\t0. No criteria (by due date, ascending order)");
                        System.out.println("\t1. Search by keyword (Title/Description)");
                        System.out.println("\t2. List by Due Date (Specific)");
                        System.out.println("\t3. List by Due Date (Range)");
                        System.out.println("\t4. List by Priority");
                        System.out.println("\t5. List by Status");
                        System.out.println("\t6. List by Project");
                        System.out.println("\t7. List by Tag");

                        switch(input2){
                            //"If no criteria, all open tasks are sorted by dueDate, ascending order"
                            default: 

                                break;
                            case 0:
                                break;
                            case 1:
                                System.out.println("Enter keyword: ");
                                String keyword = kb.nextLine();
                                System.out.println(searchTasksKeyword(keyword));
                                break;
                            case 2:
                                System.out.println("Enter Specific Due Date (DD-MM-YYYY: ");
                                // LocalDate dueDate = new LocalDate(kb.nextLine());
                            case 3:
                                break;
                            case 4:
                                System.out.println("Choose priority: ");
                                System.out.println("\t1. HIGH");
                                System.out.println("\t2. MEDIUM");
                                System.out.println("\t3. LOW");
                                int option = kb.nextInt(); kb.nextLine();
                                switch(option){
                                    case 1:
                                        searchTasksPriority(PriorityLevel.HIGH);
                                        break;
                                    case 2:
                                        searchTasksPriority(PriorityLevel.MEDIUM);
                                        break;
                                    case 3:
                                        searchTasksPriority(PriorityLevel.LOW);
                                        break;
                                }
                                break;
                            case 5: 
                                System.out.println("Choose status: ");
                                System.out.println("\t1. OPEN");
                                System.out.println("\t2. COMPLETED");
                                System.out.println("\t3. CLOSED");
                                int option2 = kb.nextInt(); kb.nextLine();  
                                switch(option2){
                                    case 1:
                                        searchTasksStatus(Status.OPEN);
                                        break;
                                    case 2:
                                        searchTasksStatus(Status.COMPLETED);
                                        break;
                                    case 3:
                                        searchTasksStatus(Status.CLOSED);
                                        break;
                                }                              
                                break;
                        }
                    }                    
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;

            }
        }

        if(kb != null)
            kb.close();
    }

    public static Task createTask(String title){
        Task t = new Task(title);
        tasks.add(t);
        return t;
    }

    public static List<Task> searchTasks(){ //NO CRITERIA -> SORTED BY DUE DATE, ASCENDING ORDER
        List<Task> results = new ArrayList<>();

        return results;
    }

    public static List<Task> searchTasksKeyword(String keyword){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getTitle().toLowerCase().contains(keyword.toLowerCase()) || t.getDescription().toLowerCase().contains(keyword.toLowerCase())){
                results.add(t);
            }
        }
        return results;
    }    

    public static List<Task> searchTasksPriority(PriorityLevel priority){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getPriorityLevel().equals(priority)){
                results.add(t);
            }
        }
        return results;
    }

    public static List<Task> searchTasksStatus(Status status){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getStatus().equals(status)){
                results.add(t);
            }
        }
        return results;
    }

    public static void updateTask(){

    }

    public static void completeTask(Task t){
        t.setStatus(Status.COMPLETED);
    }

    public static void cancelTask(Task t){
        t.setStatus(Status.CLOSED);
    }

    public static void createProject(String title){

    }

}