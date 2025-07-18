package com.fireprediction.database;

/**
 * Exception thrown when database operations fail.
 * 
 * OOP Principles:
 * - Inheritance: Extends Exception
 * - Encapsulation: Encapsulates error information
 */
public class DatabaseException extends Exception {
    
    /**
     * Constructs a DatabaseException with the specified detail message.
     * 
     * @param message the detail message
     */
    public DatabaseException(String message) {
        super(message);
    }
    
    /**
     * Constructs a DatabaseException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
