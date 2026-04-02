package com.taskmanager.cli;

public class App {

    private final TaskMenu taskMenu;
    private final ProjectMenu projectMenu;
    private final CollaboratorMenu collaboratorMenu;
    private final OverloadMenu overloadMenu;
    private final SearchMenu searchMenu;
    private final RecurrenceMenu recurrenceMenu;
    private final ExportImportMenu exportImportMenu;

    public App(TaskMenu taskMenu, ProjectMenu projectMenu, CollaboratorMenu collaboratorMenu,
               OverloadMenu overloadMenu, SearchMenu searchMenu, RecurrenceMenu recurrenceMenu,
               ExportImportMenu exportImportMenu) {
        this.taskMenu = taskMenu;
        this.projectMenu = projectMenu;
        this.collaboratorMenu = collaboratorMenu;
        this.overloadMenu = overloadMenu;
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
            ConsoleUtils.println("4. Overloaded Collaborators");
            ConsoleUtils.println("5. Search");
            ConsoleUtils.println("6. Recurring Tasks");
            ConsoleUtils.println("7. Export / Import");
            ConsoleUtils.println("0. Exit");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 7);
            switch (choice) {
                case 1 -> taskMenu.show();
                case 2 -> projectMenu.show();
                case 3 -> collaboratorMenu.show();
                case 4 -> overloadMenu.show();
                case 5 -> searchMenu.show();
                case 6 -> recurrenceMenu.show();
                case 7 -> exportImportMenu.show();
                case 0 -> {
                    ConsoleUtils.println("Goodbye.");
                    return;
                }
            }
        }
    }
}
