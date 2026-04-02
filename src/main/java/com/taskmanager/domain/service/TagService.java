package com.taskmanager.domain.service;

import com.taskmanager.domain.model.Tag;
import com.taskmanager.exception.TaskManagerException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.persistence.repository.TagRepository;

import java.util.List;
import java.util.Optional;

public class TagService {

    private final TagRepository tagRepo;

    public TagService(TagRepository tagRepo) {
        this.tagRepo = tagRepo;
    }

    /**
     * Returns existing tag with name, or creates a new one.
     */
    public Tag findOrCreate(String name) throws TaskManagerException {
        if (name == null || name.isBlank()) throw new ValidationException("Tag name cannot be empty.");
        Optional<Tag> existing = tagRepo.findByName(name.trim());
        if (existing.isPresent()) return existing.get();
        Tag tag = new Tag();
        tag.setName(name.trim());
        return tagRepo.save(tag);
    }

    public Tag getById(long id) throws TaskManagerException {
        return tagRepo.findById(id)
            .orElseThrow(() -> new ValidationException("Tag not found: " + id));
    }

    public List<Tag> listAll() throws TaskManagerException {
        return tagRepo.findAll();
    }
}
