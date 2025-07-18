package com.fireprediction.sensor;

import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages a collection of sensors and provides methods to read data from all sensors.
 * 
 * OOP Principles:
 * - Facade Pattern: Provides a simplified interface for working with sensors
 * - Composition: Composed of multiple Sensor objects
 * - Single Responsibility: Manages sensor collection and scheduling
 */
public class SensorManager {

    private static final Logger logger = LoggerFactory.getLogger(SensorManager.class);
    
    private final List<Sensor> sensors = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<SensorReading>> readingListeners = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private int readIntervalSeconds = 60; // Default to 1 minute
    private boolean running = false;
    
    /**
     * Create a new SensorManager.
     */
    public SensorManager() {
        logger.info("Initializing SensorManager");
    }
    
    /**
     * Create a new SensorManager with a specific read interval.
     * 
     * @param readIntervalSeconds interval between sensor readings in seconds
     */
    public SensorManager(int readIntervalSeconds) {
        this();
        this.readIntervalSeconds = readIntervalSeconds;
        logger.info("Setting read interval to {} seconds", readIntervalSeconds);
    }
    
    /**
     * Add a sensor to the manager.
     * 
     * @param sensor the sensor to add
     * @return true if the sensor was added successfully
     */
    public boolean addSensor(Sensor sensor) {
        logger.info("Adding sensor: {}({})", sensor.getSensorId(), sensor.getLocation());
        return sensors.add(sensor);
    }
    
    /**
     * Remove a sensor from the manager.
     * 
     * @param sensor the sensor to remove
     * @return true if the sensor was removed
     */
    public boolean removeSensor(Sensor sensor) {
        logger.info("Removing sensor: {}({})", sensor.getSensorId(), sensor.getLocation());
        return sensors.remove(sensor);
    }
    
    /**
     * Initialize all sensors.
     * 
     * @throws SensorException if initialization fails
     */
    public void initializeSensors() throws SensorException {
        logger.info("Initializing {} sensors", sensors.size());
        
        List<SensorException> exceptions = new ArrayList<>();
        
        for (Sensor sensor : sensors) {
            try {
                logger.debug("Initializing sensor: {}", sensor.getSensorId());
                sensor.initialize();
            } catch (SensorException e) {
                logger.error("Failed to initialize sensor {}: {}", 
                        sensor.getSensorId(), e.getMessage(), e);
                exceptions.add(e);
            }
        }
        
        if (!exceptions.isEmpty()) {
            SensorException firstException = exceptions.get(0);
            if (exceptions.size() > 1) {
                logger.warn("Multiple sensors failed to initialize. Reporting first exception.");
            }
            throw new SensorException("Failed to initialize sensors", firstException);
        }
    }
    
    /**
     * Read data from all sensors.
     * 
     * @return list of sensor readings
     * @throws SensorException if reading fails
     */
    public List<SensorReading> readAllSensors() throws SensorException {
        logger.debug("Reading data from all sensors");
        
        List<SensorReading> readings = new ArrayList<>();
        for (Sensor sensor : sensors) {
            try {
                if (sensor.isConnected()) {
                    SensorReading reading = sensor.readData();
                    readings.add(reading);
                    
                    // Log the reading to the console for real-time monitoring
                    logger.info("READING: Sensor={}, Location={}, Temp={}°C, Humidity={}%, Time={}", 
                            reading.getSensorId(),
                            reading.getLocation(),
                            String.format("%.1f", reading.getTemperature()),
                            String.format("%.1f", reading.getHumidity()),
                            reading.getTimestamp());
                    
                    // Notify listeners
                    for (Consumer<SensorReading> listener : readingListeners) {
                        try {
                            listener.accept(reading);
                        } catch (Exception e) {
                            logger.error("Error in reading listener: {}", e.getMessage(), e);
                        }
                    }
                } else {
                    logger.warn("Sensor not connected: {}", sensor.getSensorId());
                }
            } catch (Exception e) {
                logger.error("Error reading from sensor {}: {}", 
                        sensor.getSensorId(), e.getMessage(), e);
            }
        }
        
        return readings;
    }
    
    /**
     * Add a listener for sensor readings.
     * 
     * @param listener consumer that will receive sensor readings
     */
    public void addReadingListener(Consumer<SensorReading> listener) {
        readingListeners.add(listener);
    }
    
    /**
     * Remove a listener.
     * 
     * @param listener the listener to remove
     * @return true if the listener was removed
     */
    public boolean removeReadingListener(Consumer<SensorReading> listener) {
        return readingListeners.remove(listener);
    }
    
    /**
     * Start scheduled sensor readings.
     */
    public void startScheduledReadings() {
        if (running) {
            logger.warn("Scheduled readings already running");
            return;
        }
        
        logger.info("Starting scheduled sensor readings every {} seconds", readIntervalSeconds);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                readAllSensors();
            } catch (Exception e) {
                logger.error("Error in scheduled sensor reading: {}", e.getMessage(), e);
            }
        }, 0, readIntervalSeconds, TimeUnit.SECONDS);
        
        running = true;
    }
    
    /**
     * Stop scheduled sensor readings.
     */
    public void stopScheduledReadings() {
        if (!running) {
            logger.warn("Scheduled readings not running");
            return;
        }
        
        logger.info("Stopping scheduled sensor readings");
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
        
        running = false;
    }
    
    /**
     * Close all sensors.
     */
    public void close() {
        logger.info("Closing SensorManager");
        
        stopScheduledReadings();
        
        logger.info("Closing all sensors");
        for (Sensor sensor : sensors) {
            try {
                sensor.close();
            } catch (SensorException e) {
                logger.error("Error closing sensor {}: {}", 
                        sensor.getSensorId(), e.getMessage(), e);
            }
        }
        
        sensors.clear();
    }
    
    /**
     * Get all sensors.
     * 
     * @return list of all sensors
     */
    public List<Sensor> getSensors() {
        return Collections.unmodifiableList(sensors);
    }
    
    /**
     * Get the read interval.
     * 
     * @return read interval in seconds
     */
    public int getReadIntervalSeconds() {
        return readIntervalSeconds;
    }
    
    /**
     * Set the read interval.
     * 
     * @param readIntervalSeconds interval between sensor readings in seconds
     */
    public void setReadIntervalSeconds(int readIntervalSeconds) {
        this.readIntervalSeconds = readIntervalSeconds;
        logger.info("Updated read interval to {} seconds", readIntervalSeconds);
        
        // Restart if running
        if (running) {
            stopScheduledReadings();
            startScheduledReadings();
        }
    }
    
    /**
     * Check if scheduled readings are running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }
}
