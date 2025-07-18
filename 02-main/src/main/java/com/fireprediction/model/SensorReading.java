package com.fireprediction.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a reading from a temperature and humidity sensor.
 * This class encapsulates all data related to a single sensor measurement.
 * 
 * OOP Principles:
 * - Encapsulation: Private fields with controlled access via getters
 * - Immutability: Object cannot be modified after creation
 * - Builder Pattern: Uses a builder for flexible object creation
 */
public class SensorReading {
    
    private final String sensorId;
    private final double temperature;
    private final double humidity;
    private final String location;
    private final LocalDateTime timestamp;
    private FireRiskLevel riskLevel;
    private double riskProbability;
    
    /**
     * Private constructor used by the Builder.
     * Objects should be created using the static factory method or Builder.
     */
    private SensorReading(String sensorId, double temperature, double humidity, 
                          String location, LocalDateTime timestamp,
                          FireRiskLevel riskLevel, double riskProbability) {
        this.sensorId = sensorId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
        this.timestamp = timestamp;
        this.riskLevel = riskLevel;
        this.riskProbability = riskProbability;
    }
    
    /**
     * Static factory method to create a SensorReading.
     * 
     * @param sensorId Unique identifier for the sensor
     * @param temperature Temperature in Celsius
     * @param humidity Humidity percentage (0-100)
     * @param location Physical location of the sensor
     * @return A new SensorReading
     */
    public static SensorReading create(String sensorId, double temperature, double humidity, String location) {
        return new Builder()
                .sensorId(sensorId)
                .temperature(temperature)
                .humidity(humidity)
                .location(location)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Get the sensor ID.
     * 
     * @return the sensor ID
     */
    public String getSensorId() {
        return sensorId;
    }
    
    /**
     * Get the temperature.
     * 
     * @return the temperature in Celsius
     */
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * Get the humidity.
     * 
     * @return the humidity percentage (0-100)
     */
    public double getHumidity() {
        return humidity;
    }
    
    /**
     * Get the location.
     * 
     * @return the physical location of the sensor
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Get the timestamp.
     * 
     * @return the time when the reading was taken
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the fire risk level.
     * 
     * @return the calculated fire risk level
     */
    public FireRiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    /**
     * Set the fire risk level.
     * 
     * @param riskLevel the calculated fire risk level
     */
    public void setRiskLevel(FireRiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    /**
     * Get the fire risk probability.
     * 
     * @return the calculated probability of fire (0.0-1.0)
     */
    public double getRiskProbability() {
        return riskProbability;
    }
    
    /**
     * Set the fire risk probability.
     * 
     * @param riskProbability the calculated probability of fire (0.0-1.0)
     */
    public void setRiskProbability(double riskProbability) {
        this.riskProbability = riskProbability;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorReading that = (SensorReading) o;
        return Double.compare(that.temperature, temperature) == 0 && 
               Double.compare(that.humidity, humidity) == 0 && 
               Objects.equals(sensorId, that.sensorId) && 
               Objects.equals(location, that.location) && 
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sensorId, temperature, humidity, location, timestamp);
    }
    
    @Override
    public String toString() {
        return "SensorReading{" +
                "sensorId='" + sensorId + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                ", riskLevel=" + riskLevel +
                ", riskProbability=" + riskProbability +
                '}';
    }
    
    /**
     * Builder class for constructing SensorReading objects.
     * Provides a fluent interface for object creation.
     */
    public static class Builder {
        private String sensorId;
        private double temperature;
        private double humidity;
        private String location;
        private LocalDateTime timestamp;
        private FireRiskLevel riskLevel;
        private double riskProbability;
        
        /**
         * Set the sensor ID.
         * 
         * @param sensorId Unique identifier for the sensor
         * @return this builder for method chaining
         */
        public Builder sensorId(String sensorId) {
            this.sensorId = sensorId;
            return this;
        }
        
        /**
         * Set the temperature.
         * 
         * @param temperature Temperature in Celsius
         * @return this builder for method chaining
         */
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        /**
         * Set the humidity.
         * 
         * @param humidity Humidity percentage (0-100)
         * @return this builder for method chaining
         */
        public Builder humidity(double humidity) {
            this.humidity = humidity;
            return this;
        }
        
        /**
         * Set the location.
         * 
         * @param location Physical location of the sensor
         * @return this builder for method chaining
         */
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        /**
         * Set the timestamp.
         * 
         * @param timestamp Time when the reading was taken
         * @return this builder for method chaining
         */
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Set the fire risk level.
         * 
         * @param riskLevel Calculated fire risk level
         * @return this builder for method chaining
         */
        public Builder riskLevel(FireRiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }
        
        /**
         * Set the fire risk probability.
         * 
         * @param riskProbability Calculated probability of fire (0.0-1.0)
         * @return this builder for method chaining
         */
        public Builder riskProbability(double riskProbability) {
            this.riskProbability = riskProbability;
            return this;
        }
        
        /**
         * Build the SensorReading object.
         * 
         * @return a new SensorReading with the builder's properties
         * @throws IllegalStateException if required fields are missing
         */
        public SensorReading build() {
            if (sensorId == null || sensorId.isEmpty()) {
                throw new IllegalStateException("SensorReading must have a sensorId");
            }
            
            if (timestamp == null) {
                timestamp = LocalDateTime.now();
            }
            
            if (location == null) {
                location = "Unknown";
            }
            
            return new SensorReading(sensorId, temperature, humidity, location, timestamp, 
                                     riskLevel, riskProbability);
        }
    }
}
