package com.taskmanager.exception;

public class TaskManagerException extends Exception {
    public TaskManagerException(String message) {
        super(message);
    }
    public TaskManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
