package com.taskmanager.domain.gateway;

import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.Task;
import com.taskmanager.exception.TaskManagerException;

import java.io.OutputStream;
import java.util.List;

public interface ICalGateway {
    void exportTask(Task task, Project project, List<String> subtaskTitles, OutputStream out)
            throws TaskManagerException;

    void exportTasks(List<Task> tasks, java.util.function.Function<Long, Project> projectLookup,
                     java.util.function.Function<Long, List<String>> subtaskLookup, OutputStream out)
            throws TaskManagerException;
}
