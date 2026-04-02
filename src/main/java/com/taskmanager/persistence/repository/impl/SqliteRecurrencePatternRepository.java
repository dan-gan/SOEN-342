package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.RecurrencePattern;
import com.taskmanager.domain.model.RecurrenceType;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.RecurrencePatternRepository;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class SqliteRecurrencePatternRepository implements RecurrencePatternRepository {

    private final DatabaseManager db;

    public SqliteRecurrencePatternRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public RecurrencePattern save(RecurrencePattern pattern) throws TaskManagerException {
        String sql = "INSERT INTO recurrence_patterns (type, interval, start_date, end_date, weekdays) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pattern.getType().name());
            ps.setInt(2, pattern.getInterval());
            ps.setString(3, pattern.getStartDate().toString());
            if (pattern.getEndDate() != null) ps.setString(4, pattern.getEndDate().toString());
            else ps.setNull(4, Types.VARCHAR);
            ps.setString(5, weekdaysToString(pattern.getWeekdays()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) pattern.setId(rs.getLong(1));
            }
            return pattern;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save recurrence pattern", e);
        }
    }

    @Override
    public Optional<RecurrencePattern> findById(long id) throws TaskManagerException {
        String sql = "SELECT id, type, interval, start_date, end_date, weekdays FROM recurrence_patterns WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find recurrence pattern", e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(long id) throws TaskManagerException {
        String sql = "DELETE FROM recurrence_patterns WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to delete recurrence pattern", e);
        }
    }

    private RecurrencePattern map(ResultSet rs) throws SQLException {
        String endDateStr = rs.getString("end_date");
        String weekdaysStr = rs.getString("weekdays");
        return new RecurrencePattern(
            rs.getLong("id"),
            RecurrenceType.fromString(rs.getString("type")),
            rs.getInt("interval"),
            LocalDate.parse(rs.getString("start_date")),
            endDateStr != null ? LocalDate.parse(endDateStr) : null,
            stringToWeekdays(weekdaysStr)
        );
    }

    private String weekdaysToString(Set<DayOfWeek> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek d : weekdays) {
            if (sb.length() > 0) sb.append(",");
            sb.append(d.name());
        }
        return sb.toString();
    }

    private Set<DayOfWeek> stringToWeekdays(String s) {
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        if (s == null || s.isBlank()) return result;
        for (String part : s.split(",")) {
            result.add(DayOfWeek.valueOf(part.trim()));
        }
        return result;
    }
}
