package com.taskmanager;

import com.taskmanager.domain.model.*;
import com.taskmanager.domain.service.*;
import com.taskmanager.exception.CollaboratorOverloadException;
import com.taskmanager.exception.RecurrenceDuplicateException;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.impl.*;
import org.junit.jupiter.api.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest {

    static DatabaseManager db;
    static TaskService taskSvc;
    static ProjectService projectSvc;
    static SubtaskService subtaskSvc;
    static CollaboratorService collaboratorSvc;
    static SearchService searchSvc;
    static RecurrenceService recurrenceSvc;
    static ActivityService activitySvc;
    static TagService tagSvc;

    @BeforeAll
    static void setup() throws TaskManagerException {
        db = new DatabaseManager(":memory:");
        db.initSchema();

        var projectRepo      = new SqliteProjectRepository(db);
        var tagRepo          = new SqliteTagRepository(db);
        var taskRepo         = new SqliteTaskRepository(db);
        var subtaskRepo      = new SqliteSubtaskRepository(db);
        var collaboratorRepo = new SqliteCollaboratorRepository(db);
        var recurrenceRepo   = new SqliteRecurrencePatternRepository(db);
        var activityRepo     = new SqliteActivityEntryRepository(db);

        activitySvc     = new ActivityService(activityRepo);
        projectSvc      = new ProjectService(projectRepo);
        tagSvc          = new TagService(tagRepo);
        taskSvc         = new TaskService(taskRepo, projectRepo, tagRepo, activitySvc, db);
        subtaskSvc      = new SubtaskService(subtaskRepo, taskRepo, activitySvc);
        collaboratorSvc = new CollaboratorService(collaboratorRepo, taskRepo, subtaskSvc, activitySvc);
        searchSvc       = new SearchService(taskRepo, tagRepo);
        recurrenceSvc   = new RecurrenceService(recurrenceRepo, taskSvc, taskRepo);
    }

    @AfterAll
    static void teardown() { db.close(); }

    // ---- Phase 2: Task CRUD ----

    @Test @Order(1)
    void createAndRetrieveTask() throws TaskManagerException {
        Task task = taskSvc.createTask("Buy groceries", "Milk, eggs", Priority.MEDIUM,
            LocalDate.now().plusDays(3), null, null);
        assertTrue(task.getId() > 0);
        assertEquals("Buy groceries", task.getTitle());
        assertEquals(TaskStatus.OPEN, task.getStatus());
    }

    @Test @Order(2)
    void completeAndCancelTask() throws TaskManagerException {
        Task task = taskSvc.createTask("Task A", null, Priority.LOW, LocalDate.now(), null, null);
        taskSvc.completeTask(task.getId());
        assertEquals(TaskStatus.COMPLETED, taskSvc.getById(task.getId()).getStatus());

        Task task2 = taskSvc.createTask("Task B", null, Priority.HIGH, null, null, null);
        taskSvc.cancelTask(task2.getId());
        assertEquals(TaskStatus.CANCELLED, taskSvc.getById(task2.getId()).getStatus());
    }

    @Test @Order(3)
    void activityHistoryRecorded() throws TaskManagerException {
        Task task = taskSvc.createTask("History task", null, Priority.MEDIUM, null, null, null);
        taskSvc.updateTitle(task.getId(), "History task updated");
        taskSvc.completeTask(task.getId());
        List<ActivityEntry> history = activitySvc.getHistory(task.getId());
        assertTrue(history.size() >= 3);
    }

    // ---- Phase 3: 50 open/no-due-date limit ----

    @Test @Order(4)
    void limitOf50OpenNoDueDateEnforced() throws TaskManagerException {
        // Create tasks with due date to avoid conflicting with the 50 limit from other tests
        // First find how many open/no-due-date tasks we already have
        SearchCriteria c = new SearchCriteria();
        c.setStatus(TaskStatus.OPEN);
        List<Task> open = searchSvc.search(c);
        long noDueDate = open.stream().filter(t -> t.getDueDate() == null).count();

        // Fill up to 50
        int toCreate = (int)(50 - noDueDate);
        for (int i = 0; i < toCreate; i++) {
            taskSvc.createTask("Filler " + i, null, Priority.LOW, null, null, null);
        }

        // 51st must fail
        assertThrows(ValidationException.class,
            () -> taskSvc.createTask("One too many", null, Priority.LOW, null, null, null));
    }

    // ---- Phase 3: Subtasks ----

    @Test @Order(5)
    void subtaskMaxEnforced() throws TaskManagerException {
        Task task = taskSvc.createTask("Big task", null, Priority.HIGH, LocalDate.now().plusDays(10), null, null);
        for (int i = 0; i < 20; i++) {
            subtaskSvc.addSubtask(task.getId(), "Sub " + i);
        }
        assertThrows(ValidationException.class,
            () -> subtaskSvc.addSubtask(task.getId(), "Sub 21"));
    }

    @Test @Order(6)
    void completingSubtaskDoesNotCompleteParent() throws TaskManagerException {
        Task task = taskSvc.createTask("Parent task", null, Priority.LOW,
            LocalDate.now().plusDays(5), null, null);
        Subtask sub = subtaskSvc.addSubtask(task.getId(), "Child");
        subtaskSvc.completeSubtask(sub.getId());
        assertEquals(TaskStatus.OPEN, taskSvc.getById(task.getId()).getStatus());
    }

    // ---- Phase 3: Collaborators ----

    @Test @Order(7)
    void collaboratorLimitEnforced() throws TaskManagerException {
        Project project = projectSvc.createProject("Test Project", null);
        Collaborator senior = collaboratorSvc.addCollaboratorToProject("Alice", CollaboratorCategory.SENIOR, project.getId());

        // Create 2 open tasks with due dates to avoid the 50-limit
        for (int i = 0; i < 2; i++) {
            Task t = taskSvc.createTask("Senior task " + i, null, Priority.MEDIUM,
                LocalDate.now().plusDays(i + 1), project.getId(), null);
            collaboratorSvc.assignCollaboratorToTask(senior.getId(), t.getId());
        }
        // 3rd assignment must fail (SENIOR limit = 2)
        Task extra = taskSvc.createTask("Extra task", null, Priority.LOW,
            LocalDate.now().plusDays(5), project.getId(), null);
        assertThrows(CollaboratorOverloadException.class,
            () -> collaboratorSvc.assignCollaboratorToTask(senior.getId(), extra.getId()));
    }

    @Test @Order(8)
    void assignCollaboratorAutoCreatesSubtask() throws TaskManagerException {
        Project project = projectSvc.createProject("Collab Project", null);
        Collaborator c = collaboratorSvc.addCollaboratorToProject("Bob", CollaboratorCategory.JUNIOR, project.getId());
        Task task = taskSvc.createTask("Collab task", null, Priority.MEDIUM,
            LocalDate.now().plusDays(2), project.getId(), null);
        collaboratorSvc.assignCollaboratorToTask(c.getId(), task.getId());
        List<Subtask> subtasks = subtaskSvc.listByTask(task.getId());
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Bob")));
    }

    // ---- Phase 4: Search ----

    @Test @Order(9)
    void defaultSearchReturnsOpenTasksSortedByDueDate() throws TaskManagerException {
        List<Task> results = searchSvc.search(null);
        assertTrue(results.stream().allMatch(t -> t.getStatus() == TaskStatus.OPEN));
        // Verify sorted: due-date ascending, nulls last
        LocalDate prev = null;
        for (Task t : results) {
            if (t.getDueDate() == null) continue;
            if (prev != null) assertTrue(!t.getDueDate().isBefore(prev));
            prev = t.getDueDate();
        }
    }

    @Test @Order(10)
    void keywordSearchMatchesTitleAndDescription() throws TaskManagerException {
        taskSvc.createTask("Special keyword task", "with unique desc xyz", Priority.LOW,
            LocalDate.now().plusDays(1), null, null);
        SearchCriteria c = new SearchCriteria();
        c.setKeyword("xyz");
        List<Task> results = searchSvc.search(c);
        assertTrue(results.stream().anyMatch(t -> t.getTitle().equals("Special keyword task")));
    }

    // ---- Phase 5: Recurring tasks ----

    @Test @Order(11)
    void dailyRecurrenceGeneratesCorrectOccurrences() throws TaskManagerException {
        RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType(RecurrenceType.DAILY);
        pattern.setInterval(1);
        pattern.setStartDate(LocalDate.of(2025, 1, 1));
        pattern.setEndDate(LocalDate.of(2025, 1, 5));

        List<LocalDate> dates = recurrenceSvc.previewOccurrenceDates(pattern);
        assertEquals(5, dates.size());
        assertEquals(LocalDate.of(2025, 1, 1), dates.get(0));
        assertEquals(LocalDate.of(2025, 1, 5), dates.get(4));
    }

    @Test @Order(12)
    void weeklyRecurrenceFiltersCorrectDays() throws TaskManagerException {
        RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType(RecurrenceType.WEEKLY);
        pattern.setInterval(1);
        pattern.setStartDate(LocalDate.of(2025, 1, 6)); // Monday
        pattern.setEndDate(LocalDate.of(2025, 1, 19));
        pattern.setWeekdays(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        List<LocalDate> dates = recurrenceSvc.previewOccurrenceDates(pattern);
        // 2 weeks × 2 days = 4 occurrences
        assertEquals(4, dates.size());
        assertTrue(dates.stream().allMatch(d ->
            d.getDayOfWeek() == DayOfWeek.MONDAY || d.getDayOfWeek() == DayOfWeek.WEDNESDAY));
    }

    @Test @Order(13)
    void recurrenceDuplicateDetected() throws TaskManagerException {
        RecurrencePattern pattern = new RecurrencePattern();
        pattern.setType(RecurrenceType.DAILY);
        pattern.setInterval(1);
        pattern.setStartDate(LocalDate.of(2030, 6, 1));
        pattern.setEndDate(LocalDate.of(2030, 6, 3));

        recurrenceSvc.generateOccurrences(pattern, "Recurring ABC", null, Priority.LOW, null);

        RecurrencePattern pattern2 = new RecurrencePattern();
        pattern2.setType(RecurrenceType.DAILY);
        pattern2.setInterval(1);
        pattern2.setStartDate(LocalDate.of(2030, 6, 1));
        pattern2.setEndDate(LocalDate.of(2030, 6, 3));

        assertThrows(RecurrenceDuplicateException.class,
            () -> recurrenceSvc.generateOccurrences(pattern2, "Recurring ABC", null, Priority.LOW, null));
    }

    // ---- Overload detection ----

    @Test @Order(14)
    void overloadDetectionAfterCategoryChange() throws TaskManagerException {
        Project project = projectSvc.createProject("Overload Project", null);
        // Create an INTERMEDIATE collaborator (limit 5) and assign 5 tasks
        Collaborator c = collaboratorSvc.addCollaboratorToProject("Charlie", CollaboratorCategory.INTERMEDIATE, project.getId());
        for (int i = 0; i < 5; i++) {
            Task t = taskSvc.createTask("Overload task " + i, null, Priority.LOW,
                LocalDate.now().plusDays(i + 1), project.getId(), null);
            collaboratorSvc.assignCollaboratorToTask(c.getId(), t.getId());
        }
        // Change to SENIOR (limit 2) — should return overload warning
        List<CollaboratorOverloadWarning> warnings = collaboratorSvc.changeCategory(c.getId(), CollaboratorCategory.SENIOR);
        assertTrue(warnings.stream().anyMatch(w -> w.getCollaborator().getId() == c.getId()));
    }
}
