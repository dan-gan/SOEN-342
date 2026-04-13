package com.taskmanager;

import com.taskmanager.cli.*;
import com.taskmanager.domain.gateway.ICalGateway;
import com.taskmanager.domain.gateway.ICalGatewayImpl;
import com.taskmanager.domain.service.*;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.impl.*;

public class Main {

    public static void main(String[] args) {
        String dbPath = args.length > 0 ? args[0] : "tasks.db";
        DatabaseManager db = new DatabaseManager(dbPath);

        try {
            db.initSchema();
        } catch (TaskManagerException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            System.exit(1);
        }

        // Repositories
        SqliteProjectRepository      projectRepo      = new SqliteProjectRepository(db);
        SqliteTagRepository          tagRepo          = new SqliteTagRepository(db);
        SqliteTaskRepository         taskRepo         = new SqliteTaskRepository(db);
        SqliteSubtaskRepository      subtaskRepo      = new SqliteSubtaskRepository(db);
        SqliteCollaboratorRepository collaboratorRepo = new SqliteCollaboratorRepository(db);
        SqliteRecurrencePatternRepository recurrenceRepo = new SqliteRecurrencePatternRepository(db);
        SqliteActivityEntryRepository activityRepo    = new SqliteActivityEntryRepository(db);

        // Services
        ActivityService     activitySvc     = new ActivityService(activityRepo);
        ProjectService      projectSvc      = new ProjectService(projectRepo);
        TagService          tagSvc          = new TagService(tagRepo);
        TaskService         taskSvc         = new TaskService(taskRepo, projectRepo, tagRepo, activitySvc, db);
        SubtaskService      subtaskSvc      = new SubtaskService(subtaskRepo, taskRepo, activitySvc);
        CollaboratorService collaboratorSvc = new CollaboratorService(collaboratorRepo, taskRepo, subtaskSvc, activitySvc);
        SearchService       searchSvc       = new SearchService(taskRepo, tagRepo);
        RecurrenceService   recurrenceSvc   = new RecurrenceService(recurrenceRepo, taskSvc, taskRepo);
        CsvService          csvSvc          = new CsvService(taskSvc, projectSvc, collaboratorSvc, subtaskSvc,
                                                             tagSvc, taskRepo, collaboratorRepo, subtaskRepo, tagRepo);
        ICalGateway         icalGateway     = new ICalGatewayImpl();

        // Seed demo data if DB is empty or --seed flag passed
        boolean forceSeed = args.length > 0 && "--seed".equals(args[0]);
        try {
            boolean isEmpty = taskSvc.listAll().isEmpty();
            if (isEmpty || forceSeed) {
                if (forceSeed && !isEmpty) {
                    System.out.println("[Seeder] --seed flag detected but DB is not empty. Delete tasks.db first to reseed.");
                } else {
                    new DevDataSeeder(taskSvc, projectSvc, tagSvc, subtaskSvc,
                            collaboratorSvc, recurrenceSvc, activitySvc).seed();
                }
            }
        } catch (TaskManagerException e) {
            System.err.println("[Seeder] Warning: " + e.getMessage());
        }

        // Menus
        TaskMenu          taskMenu          = new TaskMenu(taskSvc, subtaskSvc, tagSvc, projectSvc, activitySvc);
        ProjectMenu       projectMenu       = new ProjectMenu(projectSvc, taskSvc, collaboratorSvc);
        CollaboratorMenu  collaboratorMenu  = new CollaboratorMenu(collaboratorSvc, projectSvc, taskSvc, subtaskSvc);
        SearchMenu        searchMenu        = new SearchMenu(searchSvc, projectSvc, tagSvc, csvSvc, icalGateway, subtaskSvc);
        RecurrenceMenu    recurrenceMenu    = new RecurrenceMenu(recurrenceSvc);
        ExportImportMenu  exportImportMenu  = new ExportImportMenu(csvSvc, icalGateway, searchSvc,
                                                                    projectSvc, taskSvc, subtaskSvc);

        App app = new App(taskMenu, projectMenu, collaboratorMenu,
                          searchMenu, recurrenceMenu, exportImportMenu);

        Runtime.getRuntime().addShutdownHook(new Thread(db::close));
        app.run();
    }
}
