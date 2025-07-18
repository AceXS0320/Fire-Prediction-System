package com.fireprediction.ml;

import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;

import java.util.List;

/**
 * Interface defining the contract for fire risk prediction algorithms.
 * 
 * OOP Principles:
 * - Interface Abstraction: Defines operations without implementation details
 * - Dependency Inversion: Higher-level modules depend on this abstraction
 * - Strategy Pattern: Different prediction strategies can be swapped at runtime
 */
public interface FireRiskPredictor {

    /**
     * Initialize the predictor.
     * 
     * @throws PredictorException if initialization fails
     */
    void initialize() throws PredictorException;
    
    /**
     * Train the model using historical sensor readings.
     * 
     * @param trainingData list of sensor readings for training
     * @return evaluation result containing model performance metrics
     * @throws PredictorException if training fails
     */
    ModelEvaluationResult trainModel(List<SensorReading> trainingData) throws PredictorException;
    
    /**
     * Predict the fire risk probability for a sensor reading.
     * 
     * @param reading the sensor reading to evaluate
     * @return probability of fire risk (0.0-1.0)
     * @throws PredictorException if prediction fails
     */
    double predictProbability(SensorReading reading) throws PredictorException;
    
    /**
     * Predict the fire risk level for a sensor reading.
     * 
     * @param reading the sensor reading to evaluate
     * @return the predicted fire risk level
     * @throws PredictorException if prediction fails
     */
    FireRiskLevel predictRiskLevel(SensorReading reading) throws PredictorException;
    
    /**
     * Load a previously trained model.
     * 
     * @param modelData serialized model data
     * @throws PredictorException if loading fails
     */
    void loadModel(byte[] modelData) throws PredictorException;
    
    /**
     * Save the current model.
     * 
     * @return serialized model data
     * @throws PredictorException if saving fails
     */
    byte[] saveModel() throws PredictorException;
    
    /**
     * Get the name of the algorithm used by this predictor.
     * 
     * @return algorithm name
     */
    String getAlgorithmName();
    
    /**
     * Get the display name of this predictor.
     * 
     * @return display name
     */
    String getName();
    
    /**
     * Check if the predictor is trained and ready to make predictions.
     * 
     * @return true if trained
     */
    boolean isTrained();
    
    /**
     * Get model evaluation metrics.
     * 
     * @return model evaluation result, or null if not trained
     */
    ModelEvaluationResult getModelMetrics();
}
