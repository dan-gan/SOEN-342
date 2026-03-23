import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SearchTasks {

    

    public static List<Task> searchTasks(List<Task> tasks){ //NO CRITERIA -> SORTED BY DUE DATE, ASCENDING ORDER
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getStatus().equals(Status.OPEN)){
                results.add(t);
            }
        }
        results.sort((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()));
        return results;
    }

    public static List<Task> searchTasksKeyword(List<Task> tasks, String keyword){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getTitle().toLowerCase().contains(keyword.toLowerCase()) || t.getDescription().toLowerCase().contains(keyword.toLowerCase())){
                results.add(t);
            }
        }
        return results;
    }    

    public static void searchTasksDueDateSpecific(List<Task> tasks, LocalDate dueDate){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getDueDate().equals(dueDate)){
                results.add(t);
            }
        }
        System.out.println(results);
    }

    public static void searchTasksDueDateRange(List<Task> tasks, LocalDate startDate, LocalDate endDate){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getDueDate().isAfter(startDate) && t.getDueDate().isBefore(endDate)){
                results.add(t);
            }
        }
        System.out.println(results);
    }

    public static List<Task> searchTasksPriority(List<Task> tasks, PriorityLevel priority){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getPriorityLevel().equals(priority)){
                results.add(t);
            }
        }
        System.out.println(results);
        return results;
    }

    public static List<Task> searchTasksStatus(List<Task> tasks, Status status){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getStatus().equals(status)){
                results.add(t);
            }
        }
        System.out.println(results);
        return results;
    }

    public static List<Task> searchTasksProject(List<Task> tasks, String project){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            if(t.getProject().getTitle().equals(project)){
                results.add(t);
            }
        }
        return results;
    }

    public static List<Task> searchTasksTag(List<Task> tasks, String tag){
        List<Task> results = new ArrayList<>();
        for(Task t : tasks){
            for(Tag tg : t.getTags()){
                if(tg.getName().equals(tag)){
                    results.add(t);
                    break; // Break to avoid adding the same task multiple times if it has multiple tags with the same name
                }
            }
        }
        return results;
    }



}
