package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.ActivityEntry;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.ActivityEntryRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteActivityEntryRepository implements ActivityEntryRepository {

    private final DatabaseManager db;

    public SqliteActivityEntryRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public ActivityEntry save(ActivityEntry entry) throws TaskManagerException {
        String sql = "INSERT INTO activity_entries (task_id, timestamp, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entry.getTaskId());
            ps.setString(2, entry.getTimestamp().toString());
            ps.setString(3, entry.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) entry.setId(rs.getLong(1));
            }
            return entry;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save activity entry", e);
        }
    }

    @Override
    public List<ActivityEntry> findByTaskId(long taskId) throws TaskManagerException {
        String sql = "SELECT id, task_id, timestamp, description FROM activity_entries WHERE task_id = ? ORDER BY timestamp";
        List<ActivityEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) entries.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find activity entries", e);
        }
        return entries;
    }

    private ActivityEntry map(ResultSet rs) throws SQLException {
        return new ActivityEntry(
            rs.getLong("id"),
            rs.getLong("task_id"),
            LocalDateTime.parse(rs.getString("timestamp")),
            rs.getString("description")
        );
    }
}
