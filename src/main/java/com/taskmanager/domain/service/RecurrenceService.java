package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.RecurrencePattern;
import com.taskmanager.domain.model.RecurrenceType;
import com.taskmanager.domain.model.Task;
import com.taskmanager.exception.RecurrenceDuplicateException;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.repository.RecurrencePatternRepository;
import com.taskmanager.persistence.repository.TaskRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecurrenceService {

    private final RecurrencePatternRepository recurrenceRepo;
    private final TaskService taskSvc;
    private final TaskRepository taskRepo;

    public RecurrenceService(RecurrencePatternRepository recurrenceRepo,
                             TaskService taskSvc, TaskRepository taskRepo) {
        this.recurrenceRepo = recurrenceRepo;
        this.taskSvc = taskSvc;
        this.taskRepo = taskRepo;
    }

    /**
     * Previews the dates that would be generated without persisting anything.
     */
    public List<LocalDate> previewOccurrenceDates(RecurrencePattern pattern) throws TaskManagerException {
        validatePattern(pattern);
        return generateDates(pattern);
    }

    /**
     * Persists the recurrence pattern and creates a Task for each occurrence.
     * Throws RecurrenceDuplicateException if any (title + dueDate) already exists.
     */
    public List<Task> generateOccurrences(RecurrencePattern pattern, String title,
                                          String description, Priority priority,
                                          Long projectId) throws TaskManagerException {
        validatePattern(pattern);
        if (title == null || title.isBlank()) throw new ValidationException("Task title cannot be empty.");

        List<LocalDate> dates = generateDates(pattern);
        if (dates.isEmpty()) throw new ValidationException("The recurrence pattern produces no occurrences.");

        // Pre-check uniqueness
        for (LocalDate date : dates) {
            if (taskRepo.existsByTitleAndDueDate(title.trim(), date.toString())) {
                throw new RecurrenceDuplicateException(
                    "A task with title '" + title + "' and due date " + date + " already exists.");
            }
        }

        RecurrencePattern saved = recurrenceRepo.save(pattern);

        List<Task> created = new ArrayList<>();
        for (LocalDate date : dates) {
            Task task = taskSvc.createTask(title, description, priority, date, projectId, null);
            taskSvc.setRecurrencePatternId(task.getId(), saved.getId());
            task.setRecurrencePatternId(saved.getId());
            created.add(task);
        }
        return created;
    }

    private List<LocalDate> generateDates(RecurrencePattern pattern) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate end = pattern.getEndDate();

        if (pattern.getType() == RecurrenceType.DAILY) {
            LocalDate current = pattern.getStartDate();
            while (end == null || !current.isAfter(end)) {
                dates.add(current);
                current = current.plusDays(pattern.getInterval());
                if (dates.size() > 10000) break;
            }
        } else if (pattern.getType() == RecurrenceType.WEEKLY) {
            // Iterate week by week (each week N*interval apart from start week),
            // collect days within that week that match the specified weekdays.
            LocalDate weekStart = pattern.getStartDate();
            while (end == null || !weekStart.isAfter(end)) {
                for (int d = 0; d < 7; d++) {
                    LocalDate candidate = weekStart.plusDays(d);
                    if ((end != null && candidate.isAfter(end))
                            || candidate.isBefore(pattern.getStartDate())) continue;
                    if (pattern.getWeekdays().contains(candidate.getDayOfWeek())) {
                        dates.add(candidate);
                    }
                }
                weekStart = weekStart.plusWeeks(pattern.getInterval());
                if (dates.size() > 10000) break;
            }
        } else { // MONTHLY
            LocalDate current = pattern.getStartDate();
            while (end == null || !current.isAfter(end)) {
                dates.add(current);
                current = current.plusMonths(pattern.getInterval());
                if (dates.size() > 10000) break;
            }
        }
        return dates;
    }

    private void validatePattern(RecurrencePattern pattern) throws ValidationException {
        if (pattern == null) throw new ValidationException("Recurrence pattern cannot be null.");
        if (pattern.getType() == null) throw new ValidationException("Recurrence type is required.");
        if (pattern.getInterval() <= 0) throw new ValidationException("Interval must be a positive integer.");
        if (pattern.getStartDate() == null) throw new ValidationException("Start date is required.");
        if (pattern.getEndDate() != null && !pattern.getEndDate().isAfter(pattern.getStartDate())) {
            throw new ValidationException("End date must be after start date.");
        }
        if (pattern.getType() == RecurrenceType.WEEKLY
                && (pattern.getWeekdays() == null || pattern.getWeekdays().isEmpty())) {
            throw new ValidationException("Weekly recurrence requires at least one weekday.");
        }
    }
}
