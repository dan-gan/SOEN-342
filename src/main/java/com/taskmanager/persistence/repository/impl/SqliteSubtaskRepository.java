package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.Subtask;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.SubtaskRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteSubtaskRepository implements SubtaskRepository {

    private final DatabaseManager db;

    public SqliteSubtaskRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Subtask save(Subtask subtask) throws TaskManagerException {
        String sql = "INSERT INTO subtasks (title, is_completed, task_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subtask.getTitle());
            ps.setInt(2, subtask.isCompleted() ? 1 : 0);
            ps.setLong(3, subtask.getTaskId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) subtask.setId(rs.getLong(1));
            }
            return subtask;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save subtask", e);
        }
    }

    @Override
    public Optional<Subtask> findById(long id) throws TaskManagerException {
        String sql = "SELECT id, title, is_completed, task_id FROM subtasks WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find subtask by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Subtask> findByTaskId(long taskId) throws TaskManagerException {
        String sql = "SELECT id, title, is_completed, task_id FROM subtasks WHERE task_id = ? ORDER BY id";
        List<Subtask> subtasks = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) subtasks.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find subtasks by task", e);
        }
        return subtasks;
    }

    @Override
    public int countByTaskId(long taskId) throws TaskManagerException {
        String sql = "SELECT COUNT(*) FROM subtasks WHERE task_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to count subtasks", e);
        }
        return 0;
    }

    @Override
    public void update(Subtask subtask) throws TaskManagerException {
        String sql = "UPDATE subtasks SET title = ?, is_completed = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, subtask.getTitle());
            ps.setInt(2, subtask.isCompleted() ? 1 : 0);
            ps.setLong(3, subtask.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to update subtask", e);
        }
    }

    @Override
    public void delete(long id) throws TaskManagerException {
        String sql = "DELETE FROM subtasks WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to delete subtask", e);
        }
    }

    private Subtask map(ResultSet rs) throws SQLException {
        return new Subtask(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getInt("is_completed") == 1,
            rs.getLong("task_id")
        );
    }
}
