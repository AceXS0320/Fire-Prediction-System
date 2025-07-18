package com.fireprediction.sensor;

/**
 * Exception thrown when sensor operations fail.
 * 
 * OOP Principles:
 * - Inheritance: Extends Exception
 * - Encapsulation: Encapsulates error information
 */
public class SensorException extends Exception {
    
    /**
     * Constructs a SensorException with the specified detail message.
     * 
     * @param message the detail message
     */
    public SensorException(String message) {
        super(message);
    }
    
    /**
     * Constructs a SensorException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public SensorException(String message, Throwable cause) {
        super(message, cause);
    }
}
