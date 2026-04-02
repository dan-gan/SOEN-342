package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.Collaborator;
import com.taskmanager.domain.model.CollaboratorCategory;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.CollaboratorRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteCollaboratorRepository implements CollaboratorRepository {

    private final DatabaseManager db;

    public SqliteCollaboratorRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Collaborator save(Collaborator collaborator) throws TaskManagerException {
        String sql = "INSERT INTO collaborators (name, category, project_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, collaborator.getName());
            ps.setString(2, collaborator.getCategory().name());
            ps.setLong(3, collaborator.getProjectId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) collaborator.setId(rs.getLong(1));
            }
            return collaborator;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save collaborator", e);
        }
    }

    @Override
    public Optional<Collaborator> findById(long id) throws TaskManagerException {
        String sql = "SELECT id, name, category, project_id FROM collaborators WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find collaborator by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Collaborator> findByNameAndProject(String name, long projectId) throws TaskManagerException {
        String sql = "SELECT id, name, category, project_id FROM collaborators WHERE name = ? AND project_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find collaborator by name and project", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Collaborator> findByProjectId(long projectId) throws TaskManagerException {
        String sql = "SELECT id, name, category, project_id FROM collaborators WHERE project_id = ? ORDER BY name";
        List<Collaborator> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find collaborators by project", e);
        }
        return list;
    }

    @Override
    public List<Collaborator> findAll() throws TaskManagerException {
        String sql = "SELECT id, name, category, project_id FROM collaborators ORDER BY name";
        List<Collaborator> list = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to list collaborators", e);
        }
        return list;
    }

    @Override
    public void update(Collaborator collaborator) throws TaskManagerException {
        String sql = "UPDATE collaborators SET name = ?, category = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, collaborator.getName());
            ps.setString(2, collaborator.getCategory().name());
            ps.setLong(3, collaborator.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to update collaborator", e);
        }
    }

    @Override
    public void delete(long id) throws TaskManagerException {
        String sql = "DELETE FROM collaborators WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to delete collaborator", e);
        }
    }

    @Override
    public int countOpenTasksForCollaborator(long collaboratorId) throws TaskManagerException {
        String sql = """
            SELECT COUNT(*) FROM tasks t
            JOIN task_collaborators tc ON t.id = tc.task_id
            WHERE tc.collaborator_id = ? AND t.status = 'OPEN'
        """;
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, collaboratorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to count open tasks for collaborator", e);
        }
        return 0;
    }

    @Override
    public void linkCollaboratorToTask(long collaboratorId, long taskId, long subtaskId) throws TaskManagerException {
        String sql = "INSERT INTO task_collaborators (task_id, collaborator_id, subtask_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.setLong(2, collaboratorId);
            ps.setLong(3, subtaskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to link collaborator to task", e);
        }
    }

    @Override
    public void unlinkCollaboratorFromTask(long collaboratorId, long taskId) throws TaskManagerException {
        String sql = "DELETE FROM task_collaborators WHERE collaborator_id = ? AND task_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, collaboratorId);
            ps.setLong(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to unlink collaborator from task", e);
        }
    }

    @Override
    public List<Long> findCollaboratorIdsByTaskId(long taskId) throws TaskManagerException {
        String sql = "SELECT collaborator_id FROM task_collaborators WHERE task_id = ?";
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong("collaborator_id"));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find collaborator ids for task", e);
        }
        return ids;
    }

    private Collaborator map(ResultSet rs) throws SQLException {
        return new Collaborator(
            rs.getLong("id"),
            rs.getString("name"),
            CollaboratorCategory.fromString(rs.getString("category")),
            rs.getLong("project_id")
        );
    }
}
