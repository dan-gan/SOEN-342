package com.taskmanager.persistence;

import com.taskmanager.exception.TaskManagerException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

public class DatabaseManager {

    private final String url;
    private Connection connection;

    public DatabaseManager(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    public Connection getConnection() throws TaskManagerException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to obtain database connection", e);
        }
    }

    public <T> T withTransaction(Callable<T> work) throws TaskManagerException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            T result = work.call();
            conn.commit();
            return result;
        } catch (TaskManagerException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new TaskManagerException("Transaction failed", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private void rollback(Connection conn) {
        try { conn.rollback(); } catch (SQLException ignored) {}
    }

    public void initSchema() throws TaskManagerException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER PRIMARY KEY
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS projects (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT    NOT NULL UNIQUE,
                    description TEXT
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tags (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS recurrence_patterns (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    type       TEXT    NOT NULL,
                    interval   INTEGER NOT NULL,
                    start_date TEXT    NOT NULL,
                    end_date   TEXT,
                    weekdays   TEXT
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
                    title                 TEXT    NOT NULL,
                    description           TEXT,
                    creation_date         TEXT    NOT NULL,
                    priority              TEXT    NOT NULL,
                    status                TEXT    NOT NULL DEFAULT 'OPEN',
                    due_date              TEXT,
                    project_id            INTEGER REFERENCES projects(id) ON DELETE SET NULL,
                    recurrence_pattern_id INTEGER REFERENCES recurrence_patterns(id) ON DELETE SET NULL
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS task_tags (
                    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                    tag_id  INTEGER NOT NULL REFERENCES tags(id)  ON DELETE CASCADE,
                    PRIMARY KEY (task_id, tag_id)
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS subtasks (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    title        TEXT    NOT NULL,
                    is_completed INTEGER NOT NULL DEFAULT 0,
                    task_id      INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS collaborators (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    name       TEXT    NOT NULL,
                    category   TEXT    NOT NULL,
                    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS task_collaborators (
                    task_id         INTEGER NOT NULL REFERENCES tasks(id)         ON DELETE CASCADE,
                    collaborator_id INTEGER NOT NULL REFERENCES collaborators(id) ON DELETE CASCADE,
                    subtask_id      INTEGER REFERENCES subtasks(id) ON DELETE SET NULL,
                    PRIMARY KEY (task_id, collaborator_id)
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS activity_entries (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    task_id     INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                    timestamp   TEXT    NOT NULL,
                    description TEXT    NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new TaskManagerException("Failed to initialize schema", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
