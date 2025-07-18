package com.fireprediction.ml;

import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple dummy implementation of FireRiskPredictor that uses temperature 
 * thresholds for prediction without machine learning.
 * 
 * OOP Principles:
 * - Polymorphism: Implements FireRiskPredictor interface
 * - Strategy Pattern: Can be used interchangeably with other predictors
 * - Template Method: Defines skeleton of operations
 */
public class DummyFireRiskPredictor implements FireRiskPredictor {

    private static final Logger logger = LoggerFactory.getLogger(DummyFireRiskPredictor.class);
    
    private boolean trained = false;
    private ModelEvaluationResult metrics;
    
    /**
     * Initialize the dummy predictor.
     * 
     * @throws PredictorException if initialization fails
     */
    @Override
    public void initialize() throws PredictorException {
        logger.info("Initializing DummyFireRiskPredictor");
        // No actual initialization needed for dummy predictor
        trained = true;
    }
    
    /**
     * Train the dummy model.
     * Since this is a dummy implementation, it doesn't actually train,
     * but just creates fake metrics.
     * 
     * @param trainingData list of sensor readings for training
     * @return dummy evaluation metrics
     * @throws PredictorException if training fails
     */
    @Override
    public ModelEvaluationResult trainModel(List<SensorReading> trainingData) throws PredictorException {
        logger.info("Training DummyFireRiskPredictor with {} readings", 
                trainingData != null ? trainingData.size() : 0);
        
        if (trainingData == null || trainingData.isEmpty()) {
            throw new PredictorException("No training data provided");
        }
        
        // Create dummy metrics
        Map<String, Double> precision = new HashMap<>();
        precision.put("overall", 0.85);
        
        Map<String, Double> recall = new HashMap<>();
        recall.put("overall", 0.82);
        
        Map<String, Double> f1Score = new HashMap<>();
        f1Score.put("overall", 0.83);
        
        metrics = new ModelEvaluationResult.Builder()
                .accuracy(0.84)
                .precision(precision)
                .recall(recall)
                .f1Score(f1Score)
                .auc(0.9)
                .truePositives(80)
                .falsePositives(15)
                .trueNegatives(75)
                .falseNegatives(20)
                .build();
        
        trained = true;
        logger.info("DummyFireRiskPredictor trained successfully");
        
        return metrics;
    }
    
    /**
     * Predict the fire risk probability for a sensor reading.
     * 
     * @param reading the sensor reading to evaluate
     * @return probability of fire risk (0.0-1.0)
     * @throws PredictorException if prediction fails
     */
    @Override
    public double predictProbability(SensorReading reading) throws PredictorException {
        if (!trained) {
            throw new PredictorException("Model not trained");
        }
        
        if (reading == null) {
            throw new PredictorException("Reading cannot be null");
        }
        
        // Simple temperature-based probability
        double temperature = reading.getTemperature();
        double humidity = reading.getHumidity();
        
        // Higher temperature and lower humidity = higher risk
        double temperatureComponent = Math.min(1.0, Math.max(0.0, temperature / 100.0));
        double humidityComponent = Math.min(1.0, Math.max(0.0, (100.0 - humidity) / 100.0));
        
        // Weight temperature more heavily
        double probability = (temperatureComponent * 0.7) + (humidityComponent * 0.3);
        
        logger.debug("Predicted probability: {} for temp={}, humidity={}", 
                String.format("%.4f", probability),
                String.format("%.1f", temperature),
                String.format("%.1f", humidity));
        
        return probability;
    }
    
    /**
     * Predict the fire risk level for a sensor reading.
     * 
     * @param reading the sensor reading to evaluate
     * @return the predicted fire risk level
     * @throws PredictorException if prediction fails
     */
    @Override
    public FireRiskLevel predictRiskLevel(SensorReading reading) throws PredictorException {
        double probability = predictProbability(reading);
        
        // Map probability to risk level
        if (probability < 0.25) {
            return FireRiskLevel.LOW;
        } else if (probability < 0.5) {
            return FireRiskLevel.MODERATE;
        } else if (probability < 0.75) {
            return FireRiskLevel.HIGH;
        } else {
            return FireRiskLevel.EXTREME;
        }
    }
    
    /**
     * Load a previously trained model.
     * No-op for dummy implementation.
     * 
     * @param modelData serialized model data
     * @throws PredictorException if loading fails
     */
    @Override
    public void loadModel(byte[] modelData) throws PredictorException {
        logger.info("Loading DummyFireRiskPredictor model");
        
        if (modelData == null || modelData.length == 0) {
            throw new PredictorException("Model data is empty");
        }
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(modelData);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            
            // Just read the metrics
            metrics = (ModelEvaluationResult) ois.readObject();
            trained = true;
            
        } catch (Exception e) {
            throw new PredictorException("Failed to load model: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save the current model.
     * 
     * @return serialized model data
     * @throws PredictorException if saving fails
     */
    @Override
    public byte[] saveModel() throws PredictorException {
        logger.info("Saving DummyFireRiskPredictor model");
        
        if (!trained) {
            throw new PredictorException("Model not trained");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            // Just save the metrics
            oos.writeObject(metrics);
            oos.flush();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new PredictorException("Failed to save model: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the name of the algorithm used by this predictor.
     * 
     * @return algorithm name
     */
    @Override
    public String getAlgorithmName() {
        return "Rule-based Thresholds";
    }
    
    /**
     * Get the display name of this predictor.
     * 
     * @return display name
     */
    @Override
    public String getName() {
        return "Dummy Predictor";
    }
    
    /**
     * Check if the predictor is trained and ready to make predictions.
     * 
     * @return true if trained
     */
    @Override
    public boolean isTrained() {
        return trained;
    }
    
    /**
     * Get model evaluation metrics.
     * 
     * @return model evaluation result, or null if not trained
     */
    @Override
    public ModelEvaluationResult getModelMetrics() {
        return metrics;
    }
}
