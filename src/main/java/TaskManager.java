import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class TaskManager {
    
    private static List<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {
        
        // Sample data for testing
        CSVTester.CSVTest();

        // Import tasks from CSV 
        CSVConnector.importTasks("tasks.csv");
        tasks = Database.getTasks();

        Scanner kb = new Scanner(System.in);     

        System.out.println("\nWelcome to your Personal Task Management System!");
        System.out.println("CSV file 'tasks.csv' has been imported with sample tasks for testing.");

        int input = -1; 
        while(input != 0){
            showMenu();
            input = getUserChoice(kb);
            // kb.nextLine();
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
                    while(input2 != 8){
                        input = -1;
                        showSearchMenu();
                        input2 = getUserChoice(kb);
                        switch(input2){
                            //"If no criteria, all open tasks are sorted by dueDate, ascending order"
                            default: 
                                SearchTasks.searchTasksNoCriteria(tasks, kb);
                                break;
                            case 0:
                                SearchTasks.searchAllTasks(tasks, kb);
                                break;
                            case 1:
                                System.out.println("Enter keyword: ");
                                String keyword = kb.nextLine();
                                SearchTasks.searchTasksKeyword(tasks, keyword, kb);
                                break;
                            case 2:
                                System.out.println("Enter Specific Due Date (YYYY-MM-DD): ");
                                LocalDate dueDate = LocalDate.parse(kb.nextLine());
                                SearchTasks.searchTasksDueDateSpecific(tasks, dueDate, kb);
                                break;
                            case 3:
                                System.out.println("Enter Start Due Date (YYYY-MM-DD): ");
                                LocalDate startDate = LocalDate.parse(kb.nextLine());
                                System.out.println("Enter End Due Date (YYYY-MM-DD): ");
                                LocalDate endDate = LocalDate.parse(kb.nextLine());
                                SearchTasks.searchTasksDueDateRange(tasks, startDate, endDate, kb);
                                break;
                            case 4:
                                System.out.println("Choose priority: ");
                                System.out.println("\t1. HIGH");
                                System.out.println("\t2. MEDIUM");
                                System.out.println("\t3. LOW");
                                int option = kb.nextInt(); 
                                kb.nextLine();
                                switch(option){
                                    case 1:
                                        SearchTasks.searchTasksPriority(tasks, PriorityLevel.HIGH, kb);
                                        break;
                                    case 2:
                                        SearchTasks.searchTasksPriority(tasks, PriorityLevel.MEDIUM, kb);
                                        break;
                                    case 3:
                                        SearchTasks.searchTasksPriority(tasks, PriorityLevel.LOW, kb);
                                        break;
                                }
                                break;
                            case 5: 
                                System.out.println("Choose status: ");
                                System.out.println("\t1. OPEN");
                                System.out.println("\t2. COMPLETED");
                                System.out.println("\t3. CLOSED");
                                int option2 = kb.nextInt(); 
                                kb.nextLine();  
                                switch(option2){
                                    case 1:
                                        SearchTasks.searchTasksStatus(tasks, Status.OPEN, kb);
                                        break;
                                    case 2:
                                        SearchTasks.searchTasksStatus(tasks, Status.COMPLETED, kb);
                                        break;
                                    case 3:
                                        SearchTasks.searchTasksStatus(tasks, Status.CLOSED, kb);
                                        break;
                                }                              
                                break;
                            case 6:
                                System.out.println("Enter project name: ");
                                String project = kb.nextLine();
                                SearchTasks.searchTasksProject(tasks, project, kb);
                                break;
                            case 7:
                                System.out.println("Enter tag name: ");
                                String tag = kb.nextLine();
                                SearchTasks.searchTasksTag(tasks, tag, kb);
                                break;
                            case 8:
                                break;
                        }
                    }                    
                    break;
                case 3:
                    Database.setTasks(tasks);
                    CSVConnector.exportTasks("tasks.csv");
                    break;
                case 4:
                    CSVConnector.importTasks("tasks.csv");
                    tasks = Database.getTasks();
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

    public static void showMenu(){
        System.out.println("\nChoose an option: ");
        System.out.println("\t1. Create a Task");
        System.out.println("\t2. Search Tasks");
        System.out.println("\t3. Export Tasks to CSV");
        System.out.println("\t4. Import Tasks from CSV");
        System.out.println("\t0. Exit");
    }
    public static void showSearchMenu() {
    System.out.println("Choose an option: ");
        System.out.println("\tDefault (Press a Valid Int). No criteria (by due date, ascending order)");
        System.out.println("\t0. View All Tasks");
        System.out.println("\t1. Search by keyword (Title/Description)");
        System.out.println("\t2. List by Due Date (Specific)");
        System.out.println("\t3. List by Due Date (Range)");
        System.out.println("\t4. List by Priority");
        System.out.println("\t5. List by Status");
        System.out.println("\t6. List by Project");
        System.out.println("\t7. List by Tag");
        System.out.println("\t8. Exit");
    }
    public static int getUserChoice(Scanner kb) {
        while (true) {
            try {
                String input = kb.nextLine().trim();
                int choice = Integer.parseInt(input); // Attempt to convert input to an integer
                return choice; // If successful, return the choice
            } catch (NumberFormatException e) {
                // Catch invalid input (non-numeric)
                System.out.print("Invalid input! Please enter a valid number: ");
            }
        }
    }

    public static Task createTask(String title){
        Task t = new Task(title);
        tasks.add(t);
        return t;
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