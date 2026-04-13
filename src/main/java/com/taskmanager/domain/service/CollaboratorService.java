package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Collaborator;
import com.taskmanager.domain.model.CollaboratorCategory;
import com.taskmanager.domain.model.Subtask;
import com.taskmanager.exception.CollaboratorOverloadException;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.repository.CollaboratorRepository;
import com.taskmanager.persistence.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class CollaboratorService {

    private final CollaboratorRepository collaboratorRepo;
    private final TaskRepository taskRepo;
    private final SubtaskService subtaskSvc;
    private final ActivityService activitySvc;

    public CollaboratorService(CollaboratorRepository collaboratorRepo, TaskRepository taskRepo,
                               SubtaskService subtaskSvc, ActivityService activitySvc) {
        this.collaboratorRepo = collaboratorRepo;
        this.taskRepo = taskRepo;
        this.subtaskSvc = subtaskSvc;
        this.activitySvc = activitySvc;
    }

    public Collaborator createCollaborator(String name, CollaboratorCategory category)
            throws TaskManagerException {
        if (name == null || name.isBlank()) throw new ValidationException("Collaborator name cannot be empty.");
        if (category == null) throw new ValidationException("Collaborator category is required.");
        Collaborator collaborator = new Collaborator();
        collaborator.setName(name.trim());
        collaborator.setCategory(category);
        collaborator.setProjectId(0);
        return collaboratorRepo.save(collaborator);
    }

    public void linkToProject(long collaboratorId, long projectId) throws TaskManagerException {
        Collaborator collaborator = requireCollaborator(collaboratorId);
        collaborator.setProjectId(projectId);
        collaboratorRepo.update(collaborator);
    }

    public Collaborator addCollaboratorToProject(String name, CollaboratorCategory category, long projectId)
            throws TaskManagerException {
        if (name == null || name.isBlank()) throw new ValidationException("Collaborator name cannot be empty.");
        if (category == null) throw new ValidationException("Collaborator category is required.");
        Collaborator collaborator = new Collaborator();
        collaborator.setName(name.trim());
        collaborator.setCategory(category);
        collaborator.setProjectId(projectId);
        return collaboratorRepo.save(collaborator);
    }

    /**
     * Assigns a collaborator to a task (preventive: throws if at limit).
     * Auto-creates a subtask on the task linked to the collaborator.
     */
    public Subtask assignCollaboratorToTask(long collaboratorId, long taskId) throws TaskManagerException {
        Collaborator collaborator = requireCollaborator(collaboratorId);
        taskRepo.findById(taskId).orElseThrow(() -> new ValidationException("Task not found: " + taskId));

        int openCount = collaboratorRepo.countOpenTasksForCollaborator(collaboratorId);
        int limit = collaborator.getCategory().getOpenTaskLimit();
        if (openCount >= limit) {
            throw new CollaboratorOverloadException(
                collaborator.getName() + " (" + collaborator.getCategory() + ") already has " +
                openCount + " open tasks. Limit is " + limit + "."
            );
        }

        Subtask subtask = subtaskSvc.addSubtaskInternal(taskId, collaborator.getName());
        collaboratorRepo.linkCollaboratorToTask(collaboratorId, taskId, subtask.getId());
        activitySvc.log(taskId, "Collaborator assigned: " + collaborator.getName());
        return subtask;
    }

    public void unassignCollaboratorFromTask(long collaboratorId, long taskId) throws TaskManagerException {
        requireCollaborator(collaboratorId);
        collaboratorRepo.unlinkCollaboratorFromTask(collaboratorId, taskId);
        activitySvc.log(taskId, "Collaborator unassigned: " + requireCollaborator(collaboratorId).getName());
    }

    /**
     * Changes collaborator category. Returns warnings if now overloaded (non-blocking).
     */
    public List<CollaboratorOverloadWarning> changeCategory(long collaboratorId, CollaboratorCategory newCategory)
            throws TaskManagerException {
        if (newCategory == null) throw new ValidationException("New category cannot be null.");
        Collaborator collaborator = requireCollaborator(collaboratorId);
        collaborator.setCategory(newCategory);
        collaboratorRepo.update(collaborator);
        return detectOverloadedCollaborators();
    }

    /**
     * Returns all collaborators whose open task count exceeds their category limit.
     */
    public List<CollaboratorOverloadWarning> detectOverloadedCollaborators() throws TaskManagerException {
        List<Collaborator> all = collaboratorRepo.findAll();
        List<CollaboratorOverloadWarning> warnings = new ArrayList<>();
        for (Collaborator c : all) {
            int openCount = collaboratorRepo.countOpenTasksForCollaborator(c.getId());
            if (openCount > c.getCategory().getOpenTaskLimit()) {
                warnings.add(new CollaboratorOverloadWarning(c, openCount));
            }
        }
        return warnings;
    }

    public List<CollaboratorOverloadWarning> detectOverloadedInProject(long projectId) throws TaskManagerException {
        List<Collaborator> collaborators = collaboratorRepo.findByProjectId(projectId);
        List<CollaboratorOverloadWarning> warnings = new ArrayList<>();
        for (Collaborator c : collaborators) {
            int openCount = collaboratorRepo.countOpenTasksForCollaborator(c.getId());
            if (openCount > c.getCategory().getOpenTaskLimit()) {
                warnings.add(new CollaboratorOverloadWarning(c, openCount));
            }
        }
        return warnings;
    }

    public List<Long> getAssignedTaskIds(long collaboratorId) throws TaskManagerException {
        return collaboratorRepo.findTaskIdsByCollaboratorId(collaboratorId);
    }

    public Long getSubtaskIdForTask(long collaboratorId, long taskId) throws TaskManagerException {
        return collaboratorRepo.findSubtaskIdForCollaboratorTask(collaboratorId, taskId);
    }

    public List<Collaborator> listAll() throws TaskManagerException {
        return collaboratorRepo.findAll();
    }

    public int countOpenTasks(long collaboratorId) throws TaskManagerException {
        return collaboratorRepo.countOpenTasksForCollaborator(collaboratorId);
    }

    public List<Collaborator> listByProject(long projectId) throws TaskManagerException {
        return collaboratorRepo.findByProjectId(projectId);
    }

    public Collaborator getById(long collaboratorId) throws TaskManagerException {
        return requireCollaborator(collaboratorId);
    }

    public void deleteCollaborator(long collaboratorId) throws TaskManagerException {
        requireCollaborator(collaboratorId);
        collaboratorRepo.delete(collaboratorId);
    }

    public void setCategoryLimit(CollaboratorCategory category, int limit) throws TaskManagerException {
        if (category == null) throw new ValidationException("Category cannot be null.");
        try {
            CollaboratorCategory.setLimit(category, limit);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public int getCategoryLimit(CollaboratorCategory category) throws TaskManagerException {
        if (category == null) throw new ValidationException("Category cannot be null.");
        return category.getOpenTaskLimit();
    }

    public Collaborator findOrCreate(String name, CollaboratorCategory category, long projectId)
            throws TaskManagerException {
        return collaboratorRepo.findByNameAndProject(name, projectId)
            .orElseGet(() -> {
                try {
                    return addCollaboratorToProject(name, category, projectId);
                } catch (TaskManagerException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private Collaborator requireCollaborator(long id) throws TaskManagerException {
        return collaboratorRepo.findById(id)
            .orElseThrow(() -> new ValidationException("Collaborator not found: " + id));
    }
}
