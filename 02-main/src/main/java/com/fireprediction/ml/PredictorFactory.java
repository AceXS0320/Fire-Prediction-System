package com.fireprediction.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating FireRiskPredictor instances.
 * 
 * OOP Principles:
 * - Factory Pattern: Creates objects without exposing instantiation logic
 * - Open/Closed: New predictor types can be added without modifying usage code
 * - Dependency Inversion: Clients depend on the FireRiskPredictor interface
 */
public class PredictorFactory {

    private static final Logger logger = LoggerFactory.getLogger(PredictorFactory.class);
    
    private PredictorFactory() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Create a predictor of the specified type.
     * 
     * @param typeStr string representation of the predictor type (e.g., "smile", "dummy")
     * @return the created predictor
     * @throws PredictorException if creation fails
     */
    public static FireRiskPredictor createPredictor(String typeStr) throws PredictorException {
        if (typeStr == null || typeStr.isEmpty()) {
            logger.warn("No predictor type specified, defaulting to dummy predictor");
            typeStr = "dummy";
        }
        
        try {
            // Normalize type string
            typeStr = typeStr.trim().toLowerCase();
            
            logger.info("Creating predictor of type: {}", typeStr);
            
            if (typeStr.equals("smile")) {
                return new SmileFireRiskPredictor();
            } else if (typeStr.equals("dummy")) {
                return new DummyFireRiskPredictor();
            } else {
                logger.error("Unsupported predictor type: {}", typeStr);
                throw new PredictorException("Unsupported predictor type: " + typeStr);
            }
        } catch (Exception e) {
            if (!(e instanceof PredictorException)) {
                logger.error("Error creating predictor: {}", e.getMessage(), e);
            }
            throw new PredictorException("Failed to create predictor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a predictor of the specified type.
     * 
     * @param type the predictor type
     * @return the created predictor
     * @throws PredictorException if creation fails
     */
    public static FireRiskPredictor createPredictor(PredictorType type) throws PredictorException {
        if (type == null) {
            logger.warn("Null predictor type specified, defaulting to dummy predictor");
            return createPredictor(PredictorType.DUMMY);
        }
        
        try {
            logger.info("Creating predictor of type: {}", type);
            
            switch (type) {
                case SMILE:
                    return new SmileFireRiskPredictor();
                case DUMMY:
                    return new DummyFireRiskPredictor();
                default:
                    logger.error("Unsupported predictor type: {}", type);
                    throw new PredictorException("Unsupported predictor type: " + type);
            }
        } catch (Exception e) {
            if (!(e instanceof PredictorException)) {
                logger.error("Error creating predictor: {}", e.getMessage(), e);
            }
            throw new PredictorException("Failed to create predictor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create and initialize a predictor of the specified type.
     * 
     * @param typeStr string representation of the predictor type
     * @return the created and initialized predictor
     * @throws PredictorException if creation or initialization fails
     */
    public static FireRiskPredictor createAndInitialize(String typeStr) throws PredictorException {
        FireRiskPredictor predictor = createPredictor(typeStr);
        
        try {
            logger.info("Initializing predictor");
            predictor.initialize();
            
            return predictor;
        } catch (Exception e) {
            logger.error("Error initializing predictor: {}", e.getMessage(), e);
            throw new PredictorException("Failed to initialize predictor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize an existing predictor.
     * 
     * @param predictor the predictor to initialize
     * @return true if initialization was successful
     */
    public static boolean initializePredictor(FireRiskPredictor predictor) {
        if (predictor == null) {
            logger.error("Cannot initialize null predictor");
            return false;
        }
        
        try {
            logger.info("Initializing predictor: {}", predictor.getName());
            predictor.initialize();
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize predictor: {}", e.getMessage(), e);
            return false;
        }
    }
}
