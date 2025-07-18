package com.fireprediction.sensor;

import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * A simulated sensor that generates random temperature and humidity readings.
 * Used for development, testing, and demonstrations when a real sensor is not available.
 * 
 * OOP Principles:
 * - Liskov Substitution: Can be used in place of any Sensor
 * - Encapsulation: Hides simulation details behind the Sensor interface
 * - Single Responsibility: Only simulates sensor readings
 */
public class SimulatedSensor implements Sensor {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedSensor.class);
    
    private final String sensorId;
    private final String location;
    private boolean connected = false;
    private final Random random = new Random();
    
    // Base values with seasonal adjustment
    private double baseTemperature;  // Celsius
    private double baseHumidity;     // Percentage
    
    // Variation ranges
    private final double temperatureVolatility;
    private final double humidityVolatility;
    
    /**
     * Create a simulated sensor with custom parameters.
     * 
     * @param sensorId Unique identifier for this sensor
     * @param location Physical location description
     * @param baseTemperature Base temperature around which readings fluctuate
     * @param baseHumidity Base humidity around which readings fluctuate
     * @param temperatureVolatility How much temperature varies between readings
     * @param humidityVolatility How much humidity varies between readings
     */
    public SimulatedSensor(String sensorId, String location, double baseTemperature, 
                           double baseHumidity, double temperatureVolatility, 
                           double humidityVolatility) {
        this.sensorId = sensorId;
        this.location = location;
        this.baseTemperature = baseTemperature;
        this.baseHumidity = baseHumidity;
        this.temperatureVolatility = temperatureVolatility;
        this.humidityVolatility = humidityVolatility;
    }
    
    /**
     * Create a simulated sensor with default parameters.
     * 
     * @param sensorId Unique identifier for this sensor
     * @param location Physical location description
     */
    public SimulatedSensor(String sensorId, String location) {
        this(sensorId, location, 25.0, 65.0, 0.1, 0.05);
    }
    
    /**
     * Initialize the simulated sensor.
     * 
     * @throws SensorException if initialization fails
     */
    @Override
    public void initialize() throws SensorException {
        logger.info("Initializing simulated sensor: {}", sensorId);
        this.connected = true;
        
        // Randomize base values slightly to simulate different sensor locations
        this.baseTemperature = 20 + random.nextDouble() * 10; // 20-30°C
        this.baseHumidity = 50 + random.nextDouble() * 30;    // 50-80%
        
        logger.info("Simulated sensor initialized with base temp: {}°C, humidity: {}%", 
                baseTemperature, baseHumidity);
    }
    
    /**
     * Generate a simulated sensor reading.
     * 
     * @return SensorReading with simulated temperature and humidity
     * @throws SensorException if reading fails
     */
    @Override
    public SensorReading readData() throws SensorException {
        if (!connected) {
            throw new SensorException("Simulated sensor not initialized");
        }
        
        // Generate realistic readings with some random fluctuation
        double temperature = simulateTemperature();
        double humidity = simulateHumidity();
        
        logger.debug("Simulated sensor reading: temp={}°C, humidity={}%", temperature, humidity);
        
        return SensorReading.create(sensorId, temperature, humidity, location);
    }
    
    /**
     * Simulate temperature reading with realistic fluctuation.
     * 
     * @return simulated temperature in Celsius
     */
    private double simulateTemperature() {
        // Add some random walk to base temperature to simulate changes over time
        baseTemperature += (random.nextDouble() - 0.5) * temperatureVolatility;
        
        // Keep within realistic bounds
        if (baseTemperature < -10) baseTemperature = -10;
        if (baseTemperature > 50) baseTemperature = 50;
        
        // Add small random noise
        return baseTemperature + (random.nextDouble() - 0.5) * 2 * temperatureVolatility;
    }
    
    /**
     * Simulate humidity reading with realistic fluctuation.
     * 
     * @return simulated humidity percentage
     */
    private double simulateHumidity() {
        // Humidity tends to be inversely related to temperature
        double temperatureEffect = (25 - baseTemperature) * 0.2;
        
        // Add some random walk to base humidity to simulate changes over time
        baseHumidity += (random.nextDouble() - 0.5) * humidityVolatility + temperatureEffect;
        
        // Keep within realistic bounds
        if (baseHumidity < 0) baseHumidity = 0;
        if (baseHumidity > 100) baseHumidity = 100;
        
        // Add small random noise
        return baseHumidity + (random.nextDouble() - 0.5) * 2 * humidityVolatility;
    }
    
    /**
     * Get the sensor ID.
     * 
     * @return Sensor ID
     */
    @Override
    public String getSensorId() {
        return sensorId;
    }
    
    /**
     * Get the location of this sensor.
     * 
     * @return Location description
     */
    @Override
    public String getLocation() {
        return location;
    }
    
    /**
     * Check if the simulated sensor is connected.
     * 
     * @return true if connected
     */
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Close the simulated sensor.
     * 
     * @throws SensorException if closing fails
     */
    @Override
    public void close() throws SensorException {
        connected = false;
        logger.info("Simulated sensor closed: {}", sensorId);
    }
}
