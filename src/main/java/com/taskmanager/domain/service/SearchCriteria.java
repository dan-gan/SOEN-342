package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.TaskStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class SearchCriteria {
    private LocalDate dueDate;
    private Priority priority;
    private TaskStatus status;
    private Long projectId;
    private Long tagId;
    private String keyword;
    private LocalDate dateRangeStart;
    private LocalDate dateRangeEnd;
    private DayOfWeek dayOfWeek;

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public LocalDate getDateRangeStart() { return dateRangeStart; }
    public void setDateRangeStart(LocalDate dateRangeStart) { this.dateRangeStart = dateRangeStart; }

    public LocalDate getDateRangeEnd() { return dateRangeEnd; }
    public void setDateRangeEnd(LocalDate dateRangeEnd) { this.dateRangeEnd = dateRangeEnd; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public boolean isEmpty() {
        return dueDate == null && priority == null && status == null && projectId == null
            && tagId == null && (keyword == null || keyword.isBlank())
            && dateRangeStart == null && dateRangeEnd == null && dayOfWeek == null;
    }
}
