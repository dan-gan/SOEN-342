import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SearchTasks {

    public static List<Task> searchTasksNoCriteria(List<Task> tasks, Scanner kb){ //NO CRITERIA -> SORTED BY DUE DATE, ASCENDING ORDER
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getStatus().equals(Status.OPEN)){
                results.add(t);
            }
        }
        results.sort((task1, task2) -> {
            if(task1.getDueDate() == null && task2.getDueDate() == null){
                return 0;
            } else if(task1.getDueDate() == null){
                return 1; // Tasks with no due date are considered "greater" and will be placed after tasks with due dates
            } else if(task2.getDueDate() == null){
                return -1; // Tasks with no due date are considered "greater" and will be placed after tasks with due dates
            } else {
                return task1.getDueDate().compareTo(task2.getDueDate());
            }
        });
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static void searchAllTasks(List<Task> tasks, Scanner kb){
        System.out.println(tasks);
        promptExportCSV(tasks, kb);
    }

    public static List<Task> searchTasksKeyword(List<Task> tasks, String keyword, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getTitle().toLowerCase().contains(keyword.toLowerCase()) || (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword.toLowerCase()))){
                results.add(t);
            }
        }
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }    

    public static List<Task> searchTasksDueDateSpecific(List<Task> tasks, LocalDate dueDate, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getDueDate() != null && t.getDueDate().equals(dueDate)){
                results.add(t);
            }
        }

        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static List<Task> searchTasksDueDateRange(List<Task> tasks, LocalDate startDate, LocalDate endDate, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getDueDate() != null && t.getDueDate().isAfter(startDate) && t.getDueDate().isBefore(endDate)){
                results.add(t);
            }
        }
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static List<Task> searchTasksPriority(List<Task> tasks, PriorityLevel priority, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getPriorityLevel().equals(priority)){
                results.add(t);
            }
        }
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static List<Task> searchTasksStatus(List<Task> tasks, Status status, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getStatus().equals(status)){
                results.add(t);
            }
        }
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static List<Task> searchTasksProject(List<Task> tasks, String project, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getProject() != null && t.getProject().getTitle().equals(project)){
                results.add(t);
            }
        }
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static List<Task> searchTasksTag(List<Task> tasks, String tag, Scanner kb){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getTags() != null){
                for(Tag tg : t.getTags()){
                    if(tg.getName().equals(tag)){
                        results.add(t);
                        break; // Break to avoid adding the same task multiple times if it has multiple tags with the same name
                    }
                }
            }
        }
        System.out.println(results);
        promptExportCSV(results, kb);
        return results;
    }

    public static void promptExportCSV(List<Task> tasks, Scanner kb){
        System.out.println("Would you like to export tasks to CSV? (1 for yes, 0 for no)");
        int response = TaskManager.getUserChoice(kb);
        if(response == 1){
            Database.setTasks(tasks);
            CSVConnector.exportTasks("tasks.csv");
        }
    }


}
