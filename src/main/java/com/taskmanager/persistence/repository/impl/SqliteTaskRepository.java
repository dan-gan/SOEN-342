package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.TaskRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTaskRepository implements TaskRepository {

    private final DatabaseManager db;

    public SqliteTaskRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Task save(Task task) throws TaskManagerException {
        String sql = """
            INSERT INTO tasks (title, description, creation_date, priority, status, due_date,
                               project_id, recurrence_pattern_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getCreationDate().toString());
            ps.setString(4, task.getPriority().name());
            ps.setString(5, task.getStatus().name());
            setNullableDate(ps, 6, task.getDueDate());
            setNullableLong(ps, 7, task.getProjectId());
            setNullableLong(ps, 8, task.getRecurrencePatternId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) task.setId(rs.getLong(1));
            }
            return task;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save task", e);
        }
    }

    @Override
    public Optional<Task> findById(long id) throws TaskManagerException {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find task by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Task> findAll() throws TaskManagerException {
        String sql = "SELECT * FROM tasks ORDER BY due_date ASC, id ASC";
        List<Task> tasks = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) tasks.add(map(rs));
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to list tasks", e);
        }
        return tasks;
    }

    @Override
    public List<Task> findByProjectId(long projectId) throws TaskManagerException {
        String sql = "SELECT * FROM tasks WHERE project_id = ? ORDER BY due_date ASC";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find tasks by project", e);
        }
        return tasks;
    }

    @Override
    public void update(Task task) throws TaskManagerException {
        String sql = """
            UPDATE tasks SET title = ?, description = ?, priority = ?, status = ?,
                             due_date = ?, project_id = ?, recurrence_pattern_id = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority().name());
            ps.setString(4, task.getStatus().name());
            setNullableDate(ps, 5, task.getDueDate());
            setNullableLong(ps, 6, task.getProjectId());
            setNullableLong(ps, 7, task.getRecurrencePatternId());
            ps.setLong(8, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to update task", e);
        }
    }

    @Override
    public void delete(long id) throws TaskManagerException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to delete task", e);
        }
    }

    @Override
    public int countOpenTasksWithoutDueDate() throws TaskManagerException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE status = 'OPEN' AND due_date IS NULL";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to count open tasks without due date", e);
        }
        return 0;
    }

    @Override
    public boolean existsByTitleAndDueDate(String title, String dueDate) throws TaskManagerException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE title = ? AND due_date = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, dueDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to check task uniqueness", e);
        }
        return false;
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) throws TaskManagerException {
        String sql = "SELECT * FROM tasks WHERE status = ? ORDER BY due_date ASC";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find tasks by status", e);
        }
        return tasks;
    }

    @Override
    public List<Task> findByRecurrencePatternId(long recurrencePatternId) throws TaskManagerException {
        String sql = "SELECT * FROM tasks WHERE recurrence_pattern_id = ? ORDER BY due_date ASC";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, recurrencePatternId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find tasks by recurrence pattern", e);
        }
        return tasks;
    }

    private Task map(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setCreationDate(LocalDateTime.parse(rs.getString("creation_date")));
        task.setPriority(Priority.fromString(rs.getString("priority")));
        task.setStatus(TaskStatus.fromString(rs.getString("status")));
        String dueDateStr = rs.getString("due_date");
        task.setDueDate(dueDateStr != null ? LocalDate.parse(dueDateStr) : null);
        long projectId = rs.getLong("project_id");
        task.setProjectId(rs.wasNull() ? null : projectId);
        long recId = rs.getLong("recurrence_pattern_id");
        task.setRecurrencePatternId(rs.wasNull() ? null : recId);
        return task;
    }

    private void setNullableDate(PreparedStatement ps, int idx, LocalDate date) throws SQLException {
        if (date != null) ps.setString(idx, date.toString());
        else ps.setNull(idx, Types.VARCHAR);
    }

    private void setNullableLong(PreparedStatement ps, int idx, Long value) throws SQLException {
        if (value != null) ps.setLong(idx, value);
        else ps.setNull(idx, Types.INTEGER);
    }
}
