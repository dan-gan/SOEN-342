package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.Collaborator;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.Optional;

public interface CollaboratorRepository {
    Collaborator save(Collaborator collaborator) throws TaskManagerException;
    Optional<Collaborator> findById(long id) throws TaskManagerException;
    Optional<Collaborator> findByNameAndProject(String name, long projectId) throws TaskManagerException;
    List<Collaborator> findByProjectId(long projectId) throws TaskManagerException;
    List<Collaborator> findAll() throws TaskManagerException;
    void update(Collaborator collaborator) throws TaskManagerException;
    void delete(long id) throws TaskManagerException;
    int countOpenTasksForCollaborator(long collaboratorId) throws TaskManagerException;
    void linkCollaboratorToTask(long collaboratorId, long taskId, long subtaskId) throws TaskManagerException;
    void unlinkCollaboratorFromTask(long collaboratorId, long taskId) throws TaskManagerException;
    List<Long> findCollaboratorIdsByTaskId(long taskId) throws TaskManagerException;
    List<Long> findTaskIdsByCollaboratorId(long collaboratorId) throws TaskManagerException;
    Long findSubtaskIdForCollaboratorTask(long collaboratorId, long taskId) throws TaskManagerException;
}
