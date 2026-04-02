package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.Project;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.ProjectRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteProjectRepository implements ProjectRepository {

    private final DatabaseManager db;

    public SqliteProjectRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Project save(Project project) throws TaskManagerException {
        String sql = "INSERT INTO projects (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) project.setId(rs.getLong(1));
            }
            return project;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save project", e);
        }
    }

    @Override
    public Optional<Project> findById(long id) throws TaskManagerException {
        String sql = "SELECT id, name, description FROM projects WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find project by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Project> findByName(String name) throws TaskManagerException {
        String sql = "SELECT id, name, description FROM projects WHERE name = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find project by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Project> findAll() throws TaskManagerException {
        String sql = "SELECT id, name, description FROM projects ORDER BY name";
        List<Project> projects = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) projects.add(map(rs));
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to list projects", e);
        }
        return projects;
    }

    @Override
    public void update(Project project) throws TaskManagerException {
        String sql = "UPDATE projects SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setLong(3, project.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to update project", e);
        }
    }

    @Override
    public void delete(long id) throws TaskManagerException {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to delete project", e);
        }
    }

    private Project map(ResultSet rs) throws SQLException {
        return new Project(rs.getLong("id"), rs.getString("name"), rs.getString("description"));
    }
}
