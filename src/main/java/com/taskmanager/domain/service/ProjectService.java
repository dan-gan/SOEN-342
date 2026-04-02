package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Project;
import com.taskmanager.exception.DuplicateNameException;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;

public class ProjectService {

    private final ProjectRepository projectRepo;

    public ProjectService(ProjectRepository projectRepo) {
        this.projectRepo = projectRepo;
    }

    public Project createProject(String name, String description) throws TaskManagerException {
        if (name == null || name.isBlank()) throw new ValidationException("Project name cannot be empty.");
        if (projectRepo.findByName(name).isPresent()) {
            throw new DuplicateNameException("A project with name '" + name + "' already exists.");
        }
        Project project = new Project();
        project.setName(name.trim());
        project.setDescription(description != null ? description.trim() : null);
        return projectRepo.save(project);
    }

    public Project getById(long id) throws TaskManagerException {
        return projectRepo.findById(id)
            .orElseThrow(() -> new ValidationException("Project not found: " + id));
    }

    public Optional<Project> findByName(String name) throws TaskManagerException {
        return projectRepo.findByName(name);
    }

    public List<Project> listAll() throws TaskManagerException {
        return projectRepo.findAll();
    }

    public Project updateProject(long id, String name, String description) throws TaskManagerException {
        Project project = getById(id);
        if (name != null && !name.isBlank()) {
            Optional<Project> existing = projectRepo.findByName(name.trim());
            if (existing.isPresent() && existing.get().getId() != id) {
                throw new DuplicateNameException("A project with name '" + name + "' already exists.");
            }
            project.setName(name.trim());
        }
        if (description != null) project.setDescription(description.trim());
        projectRepo.update(project);
        return project;
    }

    public void deleteProject(long id) throws TaskManagerException {
        getById(id);
        projectRepo.delete(id);
    }
}
