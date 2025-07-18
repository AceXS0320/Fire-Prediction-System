package com.fireprediction.sensor;

import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * A simulated sensor that generates dangerously high temperature readings.
 * Used for testing high-risk and extreme-risk fire conditions.
 */
public class DangerousSimulatedSensor extends SimulatedSensor {

    private static final Logger logger = LoggerFactory.getLogger(DangerousSimulatedSensor.class);
    private final boolean extremeRisk;
    private final Random random = new Random();
    
    /**
     * Create a dangerous simulated sensor.
     * 
     * @param sensorId Unique identifier for this sensor
     * @param location Physical location description  
     * @param extremeRisk If true, generates EXTREME risk temperatures (51-60°C),
     *                   if false, generates HIGH risk temperatures (41-50°C)
     */
    public DangerousSimulatedSensor(String sensorId, String location, boolean extremeRisk) {
        super(sensorId, location);
        this.extremeRisk = extremeRisk;
    }
    
    /**
     * Generate a simulated sensor reading with dangerously high temperature.
     * 
     * @return SensorReading with high-risk temperature values
     * @throws SensorException if reading fails
     */
    @Override
    public SensorReading readData() throws SensorException {
        if (!isConnected()) {
            throw new SensorException("Simulated sensor not initialized");
        }
        
        // Generate a temperature in either HIGH or EXTREME risk range
        double temperature;
        if (extremeRisk) {
            // EXTREME risk: 51-60°C
            temperature = 51 + random.nextDouble() * 9;
            logger.warn("Generating EXTREME risk temperature: {}°C", String.format("%.1f", temperature));
        } else {
            // HIGH risk: 41-50°C  
            temperature = 41 + random.nextDouble() * 9;
            logger.warn("Generating HIGH risk temperature: {}°C", String.format("%.1f", temperature));
        }
        
        // Generate a low humidity (which would be consistent with fire risk)
        double humidity = 10 + random.nextDouble() * 20; // 10-30% humidity
        
        logger.debug("Dangerous simulated sensor reading: temp={}°C, humidity={}%", 
                String.format("%.1f", temperature), 
                String.format("%.1f", humidity));
        
        return SensorReading.create(getSensorId(), temperature, humidity, getLocation());
    }
}
