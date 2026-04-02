package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Tag;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.repository.TagRepository;
import com.taskmanager.persistence.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class SearchService {

    private final TaskRepository taskRepo;
    private final TagRepository tagRepo;

    public SearchService(TaskRepository taskRepo, TagRepository tagRepo) {
        this.taskRepo = taskRepo;
        this.tagRepo = tagRepo;
    }

    /**
     * Searches tasks using the given criteria.
     * If all criteria are empty, returns all OPEN tasks sorted by due date ascending (nulls last).
     */
    public List<Task> search(SearchCriteria criteria) throws TaskManagerException {
        List<Task> all = taskRepo.findAll();

        if (criteria == null || criteria.isEmpty()) {
            List<Task> open = new ArrayList<>();
            for (Task t : all) {
                if (t.getStatus() == TaskStatus.OPEN) open.add(t);
            }
            open.sort(Comparator.comparing(Task::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
            return open;
        }

        List<Task> results = new ArrayList<>();
        for (Task t : all) {
            if (matches(t, criteria)) results.add(t);
        }
        results.sort(Comparator.comparing(Task::getDueDate,
            Comparator.nullsLast(Comparator.naturalOrder())));
        return results;
    }

    private boolean matches(Task t, SearchCriteria c) throws TaskManagerException {
        if (c.getDueDate() != null && !c.getDueDate().equals(t.getDueDate())) return false;
        if (c.getPriority() != null && t.getPriority() != c.getPriority()) return false;
        if (c.getStatus() != null && t.getStatus() != c.getStatus()) return false;
        if (c.getProjectId() != null) {
            if (t.getProjectId() == null || !t.getProjectId().equals(c.getProjectId())) return false;
        }
        if (c.getTagId() != null) {
            Set<Tag> tags = tagRepo.findByTaskId(t.getId());
            boolean hasTag = tags.stream().anyMatch(tag -> tag.getId() == c.getTagId());
            if (!hasTag) return false;
        }
        if (c.getKeyword() != null && !c.getKeyword().isBlank()) {
            String kw = c.getKeyword().toLowerCase();
            boolean titleMatch = t.getTitle() != null && t.getTitle().toLowerCase().contains(kw);
            boolean descMatch = t.getDescription() != null && t.getDescription().toLowerCase().contains(kw);
            if (!titleMatch && !descMatch) return false;
        }
        if (c.getDateRangeStart() != null && t.getDueDate() != null
                && t.getDueDate().isBefore(c.getDateRangeStart())) return false;
        if (c.getDateRangeEnd() != null && t.getDueDate() != null
                && t.getDueDate().isAfter(c.getDateRangeEnd())) return false;
        if (c.getDateRangeStart() != null && t.getDueDate() == null) return false;
        if (c.getDateRangeEnd() != null && t.getDueDate() == null) return false;
        if (c.getDayOfWeek() != null) {
            if (t.getDueDate() == null || t.getDueDate().getDayOfWeek() != c.getDayOfWeek()) return false;
        }
        return true;
    }
}
