package com.taskmanager;

import com.taskmanager.domain.model.*;
import com.taskmanager.domain.service.*;
import com.taskmanager.exception.TaskManagerException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Populates the database with representative test data covering every
 * Iteration 3 feature and its edge cases.
 *
 * Run with:  java -jar task-manager-jar-with-dependencies.jar --seed
 * Or the app auto-seeds when the database is empty on first launch.
 */
public class DevDataSeeder {

    private final TaskService         taskSvc;
    private final ProjectService      projectSvc;
    private final TagService          tagSvc;
    private final SubtaskService      subtaskSvc;
    private final CollaboratorService collaboratorSvc;
    private final RecurrenceService   recurrenceSvc;
    private final ActivityService     activitySvc;

    public DevDataSeeder(TaskService taskSvc, ProjectService projectSvc,
                         TagService tagSvc, SubtaskService subtaskSvc,
                         CollaboratorService collaboratorSvc,
                         RecurrenceService recurrenceSvc,
                         ActivityService activitySvc) {
        this.taskSvc         = taskSvc;
        this.projectSvc      = projectSvc;
        this.tagSvc          = tagSvc;
        this.subtaskSvc      = subtaskSvc;
        this.collaboratorSvc = collaboratorSvc;
        this.recurrenceSvc   = recurrenceSvc;
        this.activitySvc     = activitySvc;
    }

    public void seed() throws TaskManagerException {
        System.out.println("[Seeder] Populating demo data...");

        // ── Tags ─────────────────────────────────────────────────────────────
        Tag school  = tagSvc.findOrCreate("school");
        Tag work    = tagSvc.findOrCreate("work");
        Tag urgent  = tagSvc.findOrCreate("urgent");
        Tag personal = tagSvc.findOrCreate("personal");

        // ── Projects ─────────────────────────────────────────────────────────
        Project coursework = projectSvc.createProject("Course Assignments",
                "University coursework and homework");
        Project renovation = projectSvc.createProject("Home Renovation",
                "Tasks related to the kitchen/bathroom renovation");

        // ── Tasks: various statuses & priorities ─────────────────────────────
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusWeeks(1);
        LocalDate lastWeek = today.minusWeeks(1);

        // Normal open tasks with due dates
        Task t1 = taskSvc.createTask("Submit SOEN 342 report", "Final iteration report",
                Priority.HIGH, nextWeek, coursework.getId(), Set.of(school.getId(), urgent.getId()));

        Task t2 = taskSvc.createTask("Buy paint for kitchen", "Eggshell white, 2 gallons",
                Priority.MEDIUM, tomorrow, renovation.getId(), Set.of(personal.getId()));

        Task t3 = taskSvc.createTask("Review lecture slides",
                "Slides for chapters 7 and 8", Priority.LOW, today,
                coursework.getId(), Set.of(school.getId()));

        // Task due in the past (overdue)
        Task t4 = taskSvc.createTask("Overdue assignment", "Should have been done last week",
                Priority.HIGH, lastWeek, coursework.getId(), Set.of(school.getId(), urgent.getId()));

        // Completed task
        Task t5 = taskSvc.createTask("Install kitchen faucet", null,
                Priority.HIGH, lastWeek, renovation.getId(), null);
        taskSvc.completeTask(t5.getId());

        // Cancelled task
        Task t6 = taskSvc.createTask("Buy granite countertop", "Too expensive, cancelled",
                Priority.LOW, nextWeek, renovation.getId(), null);
        taskSvc.cancelTask(t6.getId());

        // Task no project, with tag
        Task t7 = taskSvc.createTask("Personal journal entry", null,
                Priority.LOW, tomorrow, null, Set.of(personal.getId()));

        // Tasks WITHOUT due date (count toward the 50-open-no-duedate OCL limit)
        Task t8  = taskSvc.createTask("Undated task A", "No due date set", Priority.LOW,  null, null, null);
        Task t9  = taskSvc.createTask("Undated task B", "No due date set", Priority.LOW,  null, null, null);
        Task t10 = taskSvc.createTask("Undated task C", "No due date set", Priority.MEDIUM, null, null, null);

        // ── Edge case: task approaching 20-subtask OCL limit ─────────────────
        Task bigTask = taskSvc.createTask("Task near subtask limit",
                "Has 19 subtasks — adding one more will hit the OCL cap of 20",
                Priority.MEDIUM, nextWeek, null, null);
        for (int i = 1; i <= 19; i++) {
            subtaskSvc.addSubtask(bigTask.getId(), "Subtask " + i);
        }
        // Completing some subtasks to show mixed progress
        subtaskSvc.completeSubtask(subtaskSvc.listByTask(bigTask.getId()).get(0).getId());
        subtaskSvc.completeSubtask(subtaskSvc.listByTask(bigTask.getId()).get(1).getId());

        // Normal task with a few subtasks
        Task t11 = taskSvc.createTask("Prepare presentation", "Mid-term presentation slides",
                Priority.HIGH, nextWeek, coursework.getId(), Set.of(school.getId()));
        subtaskSvc.addSubtask(t11.getId(), "Outline structure");
        subtaskSvc.addSubtask(t11.getId(), "Create slides");
        Subtask s3 = subtaskSvc.addSubtask(t11.getId(), "Rehearse");
        subtaskSvc.completeSubtask(subtaskSvc.listByTask(t11.getId()).get(0).getId());

        // ── Collaborators ─────────────────────────────────────────────────────

        // Alice — SENIOR (limit 2), assigned 1 task → can take 1 more before hitting limit
        Collaborator alice = collaboratorSvc.addCollaboratorToProject(
                "Alice", CollaboratorCategory.SENIOR, coursework.getId());
        collaboratorSvc.assignCollaboratorToTask(alice.getId(), t1.getId());

        // Bob — SENIOR (limit 2), assigned 2 tasks → AT LIMIT (next assign throws)
        Collaborator bob = collaboratorSvc.addCollaboratorToProject(
                "Bob", CollaboratorCategory.SENIOR, coursework.getId());
        collaboratorSvc.assignCollaboratorToTask(bob.getId(), t3.getId());
        collaboratorSvc.assignCollaboratorToTask(bob.getId(), t4.getId());

        // Carol — INTERMEDIATE (limit 5), assigned 2 tasks → well below limit
        Collaborator carol = collaboratorSvc.addCollaboratorToProject(
                "Carol", CollaboratorCategory.INTERMEDIATE, renovation.getId());
        collaboratorSvc.assignCollaboratorToTask(carol.getId(), t2.getId());

        // Dave — JUNIOR (limit 10 by default), assigned 2 open tasks,
        // then category limit for JUNIOR is lowered to 1 → Dave becomes OVERLOADED
        Collaborator dave = collaboratorSvc.addCollaboratorToProject(
                "Dave", CollaboratorCategory.JUNIOR, renovation.getId());
        collaboratorSvc.assignCollaboratorToTask(dave.getId(), t7.getId());
        collaboratorSvc.assignCollaboratorToTask(dave.getId(), t11.getId());
        CollaboratorCategory.setLimit(CollaboratorCategory.JUNIOR, 1); // now Dave has 2 > limit 1

        // ── Recurring tasks ───────────────────────────────────────────────────

        // Daily standup: every day for 5 days starting tomorrow
        RecurrencePattern daily = new RecurrencePattern();
        daily.setType(RecurrenceType.DAILY);
        daily.setInterval(1);
        daily.setStartDate(tomorrow);
        daily.setEndDate(tomorrow.plusDays(4));
        recurrenceSvc.generateOccurrences(daily, "Daily standup", "Team sync", Priority.LOW,
                coursework.getId());

        // Weekly: every Monday and Wednesday for 2 weeks
        RecurrencePattern weekly = new RecurrencePattern();
        weekly.setType(RecurrenceType.WEEKLY);
        weekly.setInterval(1);
        weekly.setStartDate(today.with(java.time.DayOfWeek.MONDAY));
        weekly.setEndDate(today.with(java.time.DayOfWeek.MONDAY).plusWeeks(2));
        weekly.setWeekdays(EnumSet.of(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.WEDNESDAY));
        recurrenceSvc.generateOccurrences(weekly, "Weekly lecture", null, Priority.MEDIUM,
                coursework.getId());

        System.out.println("[Seeder] Done. Summary:");
        System.out.println("  Projects : 2  (Course Assignments, Home Renovation)");
        System.out.println("  Tags     : 4  (school, work, urgent, personal)");
        System.out.println("  Tasks    : ~25 (open/completed/cancelled, with/without due dates)");
        System.out.println("  Edge cases:");
        System.out.println("    - Task ID " + bigTask.getId() + " has 19/20 subtasks (adding 1 more hits OCL limit)");
        System.out.println("    - Bob (SENIOR) is AT his 2-task limit (next assign throws)");
        System.out.println("    - Dave (JUNIOR) is OVERLOADED: 2 open tasks > limit 1");
        System.out.println("    - 3 open tasks with no due date (toward the 50-task OCL limit)");
        System.out.println("    - 1 overdue task (due last week, still OPEN)");
        System.out.println("    - Recurring daily series (5 tasks) + weekly series (4 tasks)");
    }
}
