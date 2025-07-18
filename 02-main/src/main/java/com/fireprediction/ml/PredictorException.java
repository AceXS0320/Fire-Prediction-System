package com.fireprediction.ml;

/**
 * Exception thrown when predictor operations fail.
 * 
 * OOP Principles:
 * - Inheritance: Extends Exception
 * - Encapsulation: Encapsulates error information
 */
public class PredictorException extends Exception {
    
    /**
     * Constructs a PredictorException with the specified detail message.
     * 
     * @param message the detail message
     */
    public PredictorException(String message) {
        super(message);
    }
    
    /**
     * Constructs a PredictorException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public PredictorException(String message, Throwable cause) {
        super(message, cause);
    }
}
