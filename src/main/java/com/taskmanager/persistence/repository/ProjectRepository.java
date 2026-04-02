package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.Project;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project project) throws TaskManagerException;
    Optional<Project> findById(long id) throws TaskManagerException;
    Optional<Project> findByName(String name) throws TaskManagerException;
    List<Project> findAll() throws TaskManagerException;
    void update(Project project) throws TaskManagerException;
    void delete(long id) throws TaskManagerException;
}
