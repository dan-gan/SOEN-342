package com.taskmanager.persistence.repository.impl;

import com.taskmanager.domain.model.Tag;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.persistence.DatabaseManager;
import com.taskmanager.persistence.repository.TagRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SqliteTagRepository implements TagRepository {

    private final DatabaseManager db;

    public SqliteTagRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Tag save(Tag tag) throws TaskManagerException {
        String sql = "INSERT INTO tags (name) VALUES (?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tag.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) tag.setId(rs.getLong(1));
            }
            return tag;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to save tag", e);
        }
    }

    @Override
    public Optional<Tag> findById(long id) throws TaskManagerException {
        String sql = "SELECT id, name FROM tags WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find tag by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tag> findByName(String name) throws TaskManagerException {
        String sql = "SELECT id, name FROM tags WHERE name = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find tag by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tag> findAll() throws TaskManagerException {
        String sql = "SELECT id, name FROM tags ORDER BY name";
        List<Tag> tags = new ArrayList<>();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) tags.add(map(rs));
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to list tags", e);
        }
        return tags;
    }

    @Override
    public Set<Tag> findByTaskId(long taskId) throws TaskManagerException {
        String sql = "SELECT t.id, t.name FROM tags t JOIN task_tags tt ON t.id = tt.tag_id WHERE tt.task_id = ?";
        Set<Tag> tags = new HashSet<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tags.add(map(rs));
            }
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to find tags for task", e);
        }
        return tags;
    }

    @Override
    public void linkTagToTask(long tagId, long taskId) throws TaskManagerException {
        String sql = "INSERT OR IGNORE INTO task_tags (task_id, tag_id) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.setLong(2, tagId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to link tag to task", e);
        }
    }

    @Override
    public void unlinkAllTagsFromTask(long taskId) throws TaskManagerException {
        String sql = "DELETE FROM task_tags WHERE task_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to unlink tags from task", e);
        }
    }

    @Override
    public void delete(long id) throws TaskManagerException {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to delete tag", e);
        }
    }

    private Tag map(ResultSet rs) throws SQLException {
        return new Tag(rs.getLong("id"), rs.getString("name"));
    }
}
