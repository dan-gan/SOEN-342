package com.taskmanager.cli;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.RecurrencePattern;
import com.taskmanager.domain.model.RecurrenceType;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.service.RecurrenceService;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecurrenceMenu {

    private final RecurrenceService recurrenceSvc;

    public RecurrenceMenu(RecurrenceService recurrenceSvc) {
        this.recurrenceSvc = recurrenceSvc;
    }

    public void show() {
        while (true) {
            ConsoleUtils.println("\n--- Recurring Tasks ---");
            ConsoleUtils.println("1. Create recurring task");
            ConsoleUtils.println("0. Back");
            int choice = ConsoleUtils.readInt("Choose: ", 0, 1);
            switch (choice) {
                case 1 -> handleCreate();
                case 0 -> { return; }
            }
        }
    }

    private void handleCreate() {
        try {
            String title = ConsoleUtils.readString("Task title: ");
            String desc = ConsoleUtils.readOptionalString("Description (optional): ");

            ConsoleUtils.println("Priority: 1=LOW  2=MEDIUM  3=HIGH");
            int prioChoice = ConsoleUtils.readInt("Choose: ", 1, 3);
            Priority priority = switch (prioChoice) { case 1 -> Priority.LOW; case 3 -> Priority.HIGH; default -> Priority.MEDIUM; };

            String projectIdStr = ConsoleUtils.readOptionalString("Project ID (blank for none): ");
            Long projectId = null;
            if (!projectIdStr.isBlank()) {
                try {
                    projectId = Long.parseLong(projectIdStr);
                } catch (NumberFormatException ignored) {
                    throw new ValidationException("Project ID must be a valid number.");
                }
            }

            RecurrencePattern pattern = buildPattern();
            if (pattern == null) return;

            // Preview
            List<LocalDate> preview = recurrenceSvc.previewOccurrenceDates(pattern);
            ConsoleUtils.println("This will create " + preview.size() + " occurrences.");
            if (preview.size() > 5) {
                ConsoleUtils.println("First 5 dates: " + preview.subList(0, 5));
                ConsoleUtils.println("Last date:     " + preview.get(preview.size() - 1));
            } else {
                ConsoleUtils.println("Dates: " + preview);
            }

            if (!ConsoleUtils.readYesNo("Confirm?")) return;

            List<Task> created = recurrenceSvc.generateOccurrences(
                pattern, title, desc.isBlank() ? null : desc, priority, projectId);
            ConsoleUtils.printSuccess("Created " + created.size() + " recurring task occurrences.");
        } catch (TaskManagerException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private static DayOfWeek parseDayOfWeek(String s) {
        return switch (s.toUpperCase()) {
            case "MON", "MONDAY"    -> DayOfWeek.MONDAY;
            case "TUE", "TUESDAY"   -> DayOfWeek.TUESDAY;
            case "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
            case "THU", "THURSDAY"  -> DayOfWeek.THURSDAY;
            case "FRI", "FRIDAY"    -> DayOfWeek.FRIDAY;
            case "SAT", "SATURDAY"  -> DayOfWeek.SATURDAY;
            case "SUN", "SUNDAY"    -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Unknown weekday: " + s);
        };
    }

    private RecurrencePattern buildPattern() {
        try {
            ConsoleUtils.println("Type: 1=DAILY  2=WEEKLY  3=MONTHLY");
            int typeChoice = ConsoleUtils.readInt("Choose: ", 1, 3);
            RecurrenceType type = switch (typeChoice) {
                case 1 -> RecurrenceType.DAILY;
                case 2 -> RecurrenceType.WEEKLY;
                default -> RecurrenceType.MONTHLY;
            };

            int interval = ConsoleUtils.readInt("Interval (e.g. 1 = every day/week/month): ");
            LocalDate startDate = ConsoleUtils.readDate("Start date");
            Optional<LocalDate> endDate = ConsoleUtils.readOptionalDate("End date");

            Set<DayOfWeek> weekdays = EnumSet.noneOf(DayOfWeek.class);
            if (type == RecurrenceType.WEEKLY) {
                ConsoleUtils.println("Weekdays (comma-separated: MON,TUE,WED,THU,FRI,SAT,SUN): ");
                String wdInput = ConsoleUtils.readString("Weekdays: ");
                for (String part : wdInput.split(",")) {
                    try { weekdays.add(parseDayOfWeek(part.trim())); }
                    catch (Exception ignored) { ConsoleUtils.printWarning("Unknown weekday: " + part.trim()); }
                }
                if (weekdays.isEmpty()) {
                    ConsoleUtils.printError("No valid weekdays entered.");
                    return null;
                }
            }

            RecurrencePattern pattern = new RecurrencePattern();
            pattern.setType(type);
            pattern.setInterval(interval);
            pattern.setStartDate(startDate);
            endDate.ifPresent(pattern::setEndDate);
            pattern.setWeekdays(weekdays);
            return pattern;
        } catch (Exception e) {
            ConsoleUtils.printError("Error building recurrence pattern: " + e.getMessage());
            return null;
        }
    }
}
