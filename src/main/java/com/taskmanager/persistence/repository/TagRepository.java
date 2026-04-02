package com.taskmanager.persistence.repository;

import com.taskmanager.domain.model.Tag;
import com.taskmanager.exception.TaskManagerException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository {
    Tag save(Tag tag) throws TaskManagerException;
    Optional<Tag> findById(long id) throws TaskManagerException;
    Optional<Tag> findByName(String name) throws TaskManagerException;
    List<Tag> findAll() throws TaskManagerException;
    Set<Tag> findByTaskId(long taskId) throws TaskManagerException;
    void linkTagToTask(long tagId, long taskId) throws TaskManagerException;
    void unlinkAllTagsFromTask(long taskId) throws TaskManagerException;
    void delete(long id) throws TaskManagerException;
}
