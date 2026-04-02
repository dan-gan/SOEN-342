package com.taskmanager.domain.gateway;

import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.Task;
import com.taskmanager.exception.TaskManagerException;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;

import java.io.OutputStream;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class ICalGatewayImpl implements ICalGateway {

    @Override
    public void exportTask(Task task, Project project, List<String> subtaskTitles, OutputStream out)
            throws TaskManagerException {
        exportTasks(List.of(task), id -> project, id -> subtaskTitles, out);
    }

    @Override
    public void exportTasks(List<Task> tasks, Function<Long, Project> projectLookup,
                            Function<Long, List<String>> subtaskLookup, OutputStream out)
            throws TaskManagerException {
        try {
            Calendar calendar = new Calendar();
            calendar.getProperties().add(new ProdId("-//Personal Task Manager//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(CalScale.GREGORIAN);

            UidGenerator uidGen = new RandomUidGenerator();

            for (Task task : tasks) {
                if (task.getDueDate() == null) continue; // skip tasks without due date

                Date dueDate = Date.from(
                    task.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                DateTime dtStart = new DateTime(dueDate);
                dtStart.setUtc(true);

                VEvent event = new VEvent(dtStart, task.getTitle());
                event.getProperties().add(uidGen.generateUid());

                // Build description
                StringBuilder desc = new StringBuilder();
                if (task.getDescription() != null && !task.getDescription().isBlank()) {
                    desc.append(task.getDescription()).append("\n\n");
                }
                desc.append("Priority: ").append(task.getPriority()).append("\n");
                desc.append("Status: ").append(task.getStatus()).append("\n");

                Project project = task.getProjectId() != null ? projectLookup.apply(task.getProjectId()) : null;
                if (project != null) {
                    desc.append("Project: ").append(project.getName()).append("\n");
                }

                List<String> subtasks = subtaskLookup.apply(task.getId());
                if (subtasks != null && !subtasks.isEmpty()) {
                    desc.append("\nSubtasks:\n");
                    for (String st : subtasks) {
                        desc.append("  - ").append(st).append("\n");
                    }
                }

                event.getProperties().add(new Description(desc.toString()));

                // Map status
                String icalStatus = switch (task.getStatus()) {
                    case COMPLETED -> "COMPLETED";
                    case CANCELLED -> "CANCELLED";
                    default -> "NEEDS-ACTION";
                };
                event.getProperties().add(new Status(icalStatus));

                calendar.getComponents().add(event);
            }

            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, out);
        } catch (Exception e) {
            throw new TaskManagerException("Failed to export iCal", e);
        }
    }
}
