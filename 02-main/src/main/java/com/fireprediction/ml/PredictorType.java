package com.fireprediction.ml;

/**
 * Enum defining types of fire risk predictors.
 * 
 * OOP Principles:
 * - Type Safety: Provides compile-time checking of predictor types
 */
public enum PredictorType {
    SMILE("smile"),
    DUMMY("dummy");
    
    private final String value;
    
    PredictorType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get the predictor type from a string.
     * 
     * @param value string representation of the predictor type
     * @return the corresponding PredictorType, or null if not found
     */
    public static PredictorType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        String normalized = value.trim().toLowerCase();
        for (PredictorType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }
        
        return null;
    }
}
