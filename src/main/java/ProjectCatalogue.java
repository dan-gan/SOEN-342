import java.util.ArrayList;

public class ProjectCatalogue {
    private static ArrayList<Project> projects = new ArrayList<>();

    public static Project findProjectById(int id){
        if(projects.size() <= id){
            return null;
        }
        return projects.get(id);
    }

    public static void addProject(Project project){
        projects.add(project);
    }
}
