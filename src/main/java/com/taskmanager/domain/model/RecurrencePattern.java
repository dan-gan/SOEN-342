package com.taskmanager.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

public class RecurrencePattern {
    private long id;
    private RecurrenceType type;
    private int interval;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<DayOfWeek> weekdays; // for WEEKLY type

    public RecurrencePattern() {
        this.weekdays = EnumSet.noneOf(DayOfWeek.class);
    }

    public RecurrencePattern(long id, RecurrenceType type, int interval,
                             LocalDate startDate, LocalDate endDate, Set<DayOfWeek> weekdays) {
        this.id = id;
        this.type = type;
        this.interval = interval;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weekdays = weekdays != null ? weekdays : EnumSet.noneOf(DayOfWeek.class);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public RecurrenceType getType() { return type; }
    public void setType(RecurrenceType type) { this.type = type; }

    public int getInterval() { return interval; }
    public void setInterval(int interval) { this.interval = interval; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Set<DayOfWeek> getWeekdays() { return weekdays; }
    public void setWeekdays(Set<DayOfWeek> weekdays) { this.weekdays = weekdays; }
}
