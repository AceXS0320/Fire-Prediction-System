package com.fireprediction.database;

import com.fireprediction.model.SensorReading;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface defining the contract for database operations.
 * 
 * OOP Principles:
 * - Interface Abstraction: Defines operations without implementation details
 * - Dependency Inversion: Higher-level modules depend on this abstraction
 */
public interface Database {

    /**
     * Initialize the database connection.
     * 
     * @throws DatabaseException if initialization fails
     */
    void initialize() throws DatabaseException;
    
    /**
     * Save a sensor reading to the database.
     * 
     * @param reading the sensor reading to save
     * @return true if saved successfully
     * @throws DatabaseException if save fails
     */
    boolean saveSensorReading(SensorReading reading) throws DatabaseException;
    
    /**
     * Get all sensor readings from the database.
     * 
     * @return list of sensor readings
     * @throws DatabaseException if retrieval fails
     */
    List<SensorReading> getAllSensorReadings() throws DatabaseException;
    
    /**
     * Get sensor readings from a specific time period.
     * 
     * @param startTime start of the time period
     * @param endTime end of the time period
     * @return list of sensor readings within the time period
     * @throws DatabaseException if retrieval fails
     */
    List<SensorReading> getSensorReadings(LocalDateTime startTime, LocalDateTime endTime) 
            throws DatabaseException;
    
    /**
     * Get readings from a specific sensor.
     * 
     * @param sensorId ID of the sensor
     * @return list of readings from the specified sensor
     * @throws DatabaseException if retrieval fails
     */
    List<SensorReading> getReadingsForSensor(String sensorId) throws DatabaseException;
    
    /**
     * Save the trained model to the database.
     * 
     * @param modelData serialized model data
     * @param modelName name of the model
     * @return true if saved successfully
     * @throws DatabaseException if save fails
     */
    boolean saveModel(byte[] modelData, String modelName) throws DatabaseException;
    
    /**
     * Load a trained model from the database.
     * 
     * @param modelName name of the model to load
     * @return serialized model data
     * @throws DatabaseException if load fails
     */
    byte[] loadModel(String modelName) throws DatabaseException;
    
    /**
     * Check if the database connection is active.
     * 
     * @return true if connected
     */
    boolean isConnected();
    
    /**
     * Close the database connection.
     * 
     * @throws DatabaseException if closing fails
     */
    void close() throws DatabaseException;
}
