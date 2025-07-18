package com.fireprediction.model;

import javafx.scene.paint.Color;

/**
 * Enum representing different fire risk levels with associated temperature thresholds,
 * descriptions, and UI colors.
 * 
 * OOP Principles: Encapsulation of related data and behavior in an enum
 */
public enum FireRiskLevel {
    LOW(0, 30, "Low fire risk", "No action required", Color.GREEN),
    MODERATE(31, 40, "Moderate fire risk", "Maintain awareness", Color.YELLOW),
    HIGH(41, 50, "High fire risk", "Implement fire prevention measures", Color.ORANGE),
    EXTREME(51, 100, "Extreme fire risk", "Evacuate immediately", Color.RED);

    private final int minTemperature;
    private final int maxTemperature;
    private final String description;
    private final String recommendedAction;
    private final Color displayColor;

    /**
     * Constructor for FireRiskLevel
     * 
     * @param minTemperature Minimum temperature threshold in Celsius
     * @param maxTemperature Maximum temperature threshold in Celsius
     * @param description Human-readable description of the risk level
     * @param recommendedAction Recommended action based on risk level
     * @param displayColor Color for UI representation
     */
    FireRiskLevel(int minTemperature, int maxTemperature, String description, 
                  String recommendedAction, Color displayColor) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.displayColor = displayColor;
    }

    /**
     * Determine the appropriate fire risk level based on temperature.
     * 
     * @param temperature The temperature in Celsius
     * @return The corresponding FireRiskLevel
     */
    public static FireRiskLevel fromTemperature(double temperature) {
        for (FireRiskLevel level : values()) {
            if (temperature >= level.minTemperature && temperature <= level.maxTemperature) {
                return level;
            }
        }
        return temperature > EXTREME.maxTemperature ? EXTREME : LOW;
    }

    /**
     * Determines if immediate action is required based on the risk level.
     * 
     * @return true if this risk level requires immediate action
     */
    public boolean requiresImmediateAction() {
        return this == HIGH || this == EXTREME;
    }
    
    /**
     * Get the minimum temperature for this risk level.
     * 
     * @return the minimum temperature in Celsius
     */
    public int getMinTemperature() {
        return minTemperature;
    }

    /**
     * Get the maximum temperature for this risk level.
     * 
     * @return the maximum temperature in Celsius
     */
    public int getMaxTemperature() {
        return maxTemperature;
    }

    /**
     * Get the description of this risk level.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the recommended action for this risk level.
     * 
     * @return the recommended action
     */
    public String getRecommendedAction() {
        return recommendedAction;
    }

    /**
     * Get the display color for this risk level.
     * 
     * @return the display color
     */
    public Color getDisplayColor() {
        return displayColor;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
