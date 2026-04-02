package com.taskmanager.exception;

public class DuplicateNameException extends TaskManagerException {
    public DuplicateNameException(String message) {
        super(message);
    }
}
