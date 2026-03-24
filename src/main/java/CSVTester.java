import java.time.LocalDate;

public class CSVTester {
    public static void CSVTest() {
        Database.reset();

        // Create and add Projects
        Project personalProject = new Project("Personal", "Personal tasks and activities");
        Project workProject = new Project("Work", "Work-related tasks");
        Project courseProject = new Project("Course", "Course-related tasks");
        Project researchProject = new Project("Research", "Research project");
        Project teamProject = new Project("Team Activities", "Tasks for team activities");
        Project softwareProject = new Project("Software", "Software development tasks");

        Database.addProject(personalProject);
        Database.addProject(workProject);
        Database.addProject(courseProject);
        Database.addProject(researchProject);
        Database.addProject(teamProject);
        Database.addProject(softwareProject);

        // Create Collaborators
        Collaborator alice = new Collaborator("Alice", CollaboratorCategory.JUNIOR);
        Collaborator bob = new Collaborator("Bob", CollaboratorCategory.SENIOR);
        Collaborator charlie = new Collaborator("Charlie", CollaboratorCategory.INTERMEDIATE);

        // Add Collaborators to Projects
        workProject.addCollaborator(alice);
        courseProject.addCollaborator(bob);
        researchProject.addCollaborator(alice);
        researchProject.addCollaborator(bob);
        teamProject.addCollaborator(charlie);
        softwareProject.addCollaborator(alice);

        // Create Tasks
        Task task1 = new Task("Task with no description");
        task1.setStatus(Status.OPEN);
        task1.setPriorityLevel(PriorityLevel.LOW);
        task1.setDueDate(LocalDate.parse("2026-03-30"));
        task1.setProject(personalProject);
        Database.addTask(task1);

        Task task2 = new Task("Write Report");
        task2.setStatus(Status.COMPLETED);
        task2.setPriorityLevel(PriorityLevel.HIGH);
        task2.setDueDate(LocalDate.parse("2026-04-05"));
        task2.setProject(workProject);
        task2.addSubtask(new Subtask("Use-case diagram"));
        task2.addSubtask(new Subtask("Class diagram"));
        task2.getProject().addCollaborator(alice);
        Database.addTask(task2);

        Task task3 = new Task("Create Presentation");
        task3.setStatus(Status.OPEN);
        task3.setPriorityLevel(PriorityLevel.MEDIUM);
        task3.setProject(courseProject);
        task3.setDueDate(null); // No due date
        task3.getProject().addCollaborator(bob);
        Database.addTask(task3);

        Task task4 = new Task("Research Topic");
        task4.setStatus(Status.OPEN);
        task4.setPriorityLevel(PriorityLevel.MEDIUM);
        task4.setDueDate(LocalDate.parse("2026-04-15"));
        task4.setProject(researchProject);
        task4.getProject().addCollaborator(alice);
        task4.getProject().addCollaborator(bob);
        Database.addTask(task4);

        Task task5 = new Task("Update Website");
        task5.setStatus(Status.CLOSED);
        task5.setPriorityLevel(PriorityLevel.LOW);
        task5.setDueDate(LocalDate.parse("2026-03-20"));
        task5.setProject(personalProject);
        Database.addTask(task5);

        Task task6 = new Task("Team Meeting");
        task6.setStatus(Status.COMPLETED);
        task6.setPriorityLevel(PriorityLevel.HIGH);
        task6.setDueDate(LocalDate.parse("2026-03-25"));
        task6.setProject(teamProject);
        task6.getProject().addCollaborator(charlie);
        Database.addTask(task6);

        Task task7 = new Task("Fix Bugs");
        task7.setStatus(Status.OPEN);
        task7.setPriorityLevel(PriorityLevel.LOW);
        task7.setDueDate(LocalDate.parse("2026-04-10"));
        task7.setProject(softwareProject);
        task7.getProject().addCollaborator(alice);
        task7.addTag(new Tag("bug"));
        task7.addTag(new Tag("urgent"));
        Database.addTask(task7);

        // Export tasks to CSV
        CSVConnector.exportTasks("tasks.csv");

        // Reset DB and Import CSV
        Database.reset();
        CSVConnector.importTasks("tasks.csv");

        // Verify imported tasks
        Database.getTasks().forEach(task -> System.out.println(task.getTitle() + " | Project: " + (task.getProject() != null ? task.getProject().getTitle() : "None")));    
    }
}
