package com.fireprediction.sensor;

import com.fireprediction.model.SensorReading;

/**
 * Interface defining the contract for sensor operations.
 * 
 * OOP Principles:
 * - Interface Abstraction: Defines a contract without implementation details
 * - Dependency Inversion: Higher-level modules depend on this abstraction, not concrete implementations
 */
public interface Sensor {

    /**
     * Initialize the sensor and establish any necessary connections.
     * 
     * @throws SensorException if initialization fails
     */
    void initialize() throws SensorException;
    
    /**
     * Read current data from the sensor.
     * 
     * @return SensorReading containing temperature and humidity data
     * @throws SensorException if reading fails
     */
    SensorReading readData() throws SensorException;
    
    /**
     * Get the unique identifier for this sensor.
     * 
     * @return Sensor ID
     */
    String getSensorId();
    
    /**
     * Get the location of this sensor.
     * 
     * @return Location description
     */
    String getLocation();
    
    /**
     * Check if the sensor is connected.
     * 
     * @return true if connected
     */
    boolean isConnected();
    
    /**
     * Close the sensor connection.
     * 
     * @throws SensorException if closing fails
     */
    void close() throws SensorException;
}
