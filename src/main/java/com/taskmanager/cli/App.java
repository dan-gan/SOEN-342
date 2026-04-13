package com.taskmanager.cli;

public class App {

    private final TaskMenu taskMenu;
    private final ProjectMenu projectMenu;
    private final CollaboratorMenu collaboratorMenu;
    private final SearchMenu searchMenu;
    private final RecurrenceMenu recurrenceMenu;
    private final ExportImportMenu exportImportMenu;

    public App(TaskMenu taskMenu, ProjectMenu projectMenu, CollaboratorMenu collaboratorMenu,
               SearchMenu searchMenu, RecurrenceMenu recurrenceMenu,
               ExportImportMenu exportImportMenu) {
        this.taskMenu = taskMenu;
        this.projectMenu = projectMenu;
        this.collaboratorMenu = collaboratorMenu;
        this.searchMenu = searchMenu;
        this.recurrenceMenu = recurrenceMenu;
        this.exportImportMenu = exportImportMenu;
    }

    public void run() {
        ConsoleUtils.println("=================================");
        ConsoleUtils.println("  Personal Task Management System");
        ConsoleUtils.println("=================================");
        while (true) {
            ConsoleUtils.println("\n--- Main Menu ---");
            ConsoleUtils.println("1. Tasks");
            ConsoleUtils.println("2. Projects");
            ConsoleUtils.println("3. Collaborators");
            ConsoleUtils.println("4. Search");
            ConsoleUtils.println("5. Recurring Tasks");
            ConsoleUtils.println("6. Export / Import");
            ConsoleUtils.println("0. Exit");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 6);
            switch (choice) {
                case 1 -> taskMenu.show();
                case 2 -> projectMenu.show();
                case 3 -> collaboratorMenu.show();
                case 4 -> searchMenu.show();
                case 5 -> recurrenceMenu.show();
                case 6 -> exportImportMenu.show();
                case 0 -> {
                    ConsoleUtils.println("Goodbye.");
                    return;
                }
            }
        }
    }
}
