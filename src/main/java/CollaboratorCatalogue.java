import java.util.ArrayList;
import java.util.List;

public class CollaboratorCatalogue {
    private static List<Collaborator> collaborators = new ArrayList<>();

    public static List<Collaborator> getCollaborators() {
        return collaborators;
    }

    public static void setCollaborators(List<Collaborator> newCollaborators) {
        collaborators = newCollaborators;
    }
}
