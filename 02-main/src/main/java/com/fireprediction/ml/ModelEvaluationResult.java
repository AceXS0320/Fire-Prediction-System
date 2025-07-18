package com.fireprediction.ml;

import java.util.Map;
import java.util.Objects;

/**
 * Class that holds the result of a machine learning model evaluation.
 * Contains various performance metrics to assess model quality.
 * 
 * OOP Principles:
 * - Encapsulation: Stores related data together
 * - Immutability: Object cannot be modified after creation
 * - Builder Pattern: Uses a builder for flexible object creation
 */
public class ModelEvaluationResult {
    
    private final double accuracy;
    private final Map<String, Double> precision;
    private final Map<String, Double> recall;
    private final Map<String, Double> f1Score;
    private final double auc;
    private final int[][] confusionMatrix;
    private final int truePositives;
    private final int falsePositives;
    private final int trueNegatives;
    private final int falseNegatives;
    
    /**
     * Private constructor used by the Builder.
     */
    private ModelEvaluationResult(double accuracy, Map<String, Double> precision, 
                                 Map<String, Double> recall, Map<String, Double> f1Score,
                                 double auc, int[][] confusionMatrix, 
                                 int truePositives, int falsePositives, 
                                 int trueNegatives, int falseNegatives) {
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.f1Score = f1Score;
        this.auc = auc;
        this.confusionMatrix = confusionMatrix;
        this.truePositives = truePositives;
        this.falsePositives = falsePositives;
        this.trueNegatives = trueNegatives;
        this.falseNegatives = falseNegatives;
    }
    
    /**
     * Get the model accuracy.
     * 
     * @return the accuracy (0.0-1.0)
     */
    public double getAccuracy() {
        return accuracy;
    }
    
    /**
     * Get the precision by class.
     * 
     * @return map of class names to precision values
     */
    public Map<String, Double> getPrecision() {
        return precision;
    }
    
    /**
     * Get the recall by class.
     * 
     * @return map of class names to recall values
     */
    public Map<String, Double> getRecall() {
        return recall;
    }
    
    /**
     * Get the F1 score by class.
     * 
     * @return map of class names to F1 score values
     */
    public Map<String, Double> getF1Score() {
        return f1Score;
    }
    
    /**
     * Get the AUC (Area Under Curve).
     * 
     * @return the AUC value (0.0-1.0)
     */
    public double getAuc() {
        return auc;
    }
    
    /**
     * Get the confusion matrix.
     * 
     * @return the confusion matrix
     */
    public int[][] getConfusionMatrix() {
        return confusionMatrix;
    }
    
    /**
     * Get the number of true positives.
     * 
     * @return the true positives count
     */
    public int getTruePositives() {
        return truePositives;
    }
    
    /**
     * Get the number of false positives.
     * 
     * @return the false positives count
     */
    public int getFalsePositives() {
        return falsePositives;
    }
    
    /**
     * Get the number of true negatives.
     * 
     * @return the true negatives count
     */
    public int getTrueNegatives() {
        return trueNegatives;
    }
    
    /**
     * Get the number of false negatives.
     * 
     * @return the false negatives count
     */
    public int getFalseNegatives() {
        return falseNegatives;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelEvaluationResult that = (ModelEvaluationResult) o;
        return Double.compare(that.accuracy, accuracy) == 0 &&
               Double.compare(that.auc, auc) == 0 &&
               truePositives == that.truePositives &&
               falsePositives == that.falsePositives &&
               trueNegatives == that.trueNegatives &&
               falseNegatives == that.falseNegatives;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accuracy, auc, truePositives, falsePositives, 
                            trueNegatives, falseNegatives);
    }
    
    @Override
    public String toString() {
        return "ModelEvaluationResult{" +
               "accuracy=" + accuracy +
               ", precision=" + precision +
               ", recall=" + recall +
               ", f1Score=" + f1Score +
               ", auc=" + auc +
               ", truePositives=" + truePositives +
               ", falsePositives=" + falsePositives +
               ", trueNegatives=" + trueNegatives +
               ", falseNegatives=" + falseNegatives +
               '}';
    }
    
    /**
     * Builder class for constructing ModelEvaluationResult objects.
     * Provides a fluent interface for object creation.
     */
    public static class Builder {
        private double accuracy;
        private Map<String, Double> precision;
        private Map<String, Double> recall;
        private Map<String, Double> f1Score;
        private double auc;
        private int[][] confusionMatrix;
        private int truePositives;
        private int falsePositives;
        private int trueNegatives;
        private int falseNegatives;
        
        /**
         * Set the accuracy.
         * 
         * @param accuracy the model accuracy (0.0-1.0)
         * @return this builder for method chaining
         */
        public Builder accuracy(double accuracy) {
            this.accuracy = accuracy;
            return this;
        }
        
        /**
         * Set the precision by class.
         * 
         * @param precision map of class names to precision values
         * @return this builder for method chaining
         */
        public Builder precision(Map<String, Double> precision) {
            this.precision = precision;
            return this;
        }
        
        /**
         * Set the recall by class.
         * 
         * @param recall map of class names to recall values
         * @return this builder for method chaining
         */
        public Builder recall(Map<String, Double> recall) {
            this.recall = recall;
            return this;
        }
        
        /**
         * Set the F1 score by class.
         * 
         * @param f1Score map of class names to F1 score values
         * @return this builder for method chaining
         */
        public Builder f1Score(Map<String, Double> f1Score) {
            this.f1Score = f1Score;
            return this;
        }
        
        /**
         * Set the AUC (Area Under Curve).
         * 
         * @param auc the AUC value (0.0-1.0)
         * @return this builder for method chaining
         */
        public Builder auc(double auc) {
            this.auc = auc;
            return this;
        }
        
        /**
         * Set the confusion matrix.
         * 
         * @param confusionMatrix the confusion matrix
         * @return this builder for method chaining
         */
        public Builder confusionMatrix(int[][] confusionMatrix) {
            this.confusionMatrix = confusionMatrix;
            return this;
        }
        
        /**
         * Set the number of true positives.
         * 
         * @param truePositives the true positives count
         * @return this builder for method chaining
         */
        public Builder truePositives(int truePositives) {
            this.truePositives = truePositives;
            return this;
        }
        
        /**
         * Set the number of false positives.
         * 
         * @param falsePositives the false positives count
         * @return this builder for method chaining
         */
        public Builder falsePositives(int falsePositives) {
            this.falsePositives = falsePositives;
            return this;
        }
        
        /**
         * Set the number of true negatives.
         * 
         * @param trueNegatives the true negatives count
         * @return this builder for method chaining
         */
        public Builder trueNegatives(int trueNegatives) {
            this.trueNegatives = trueNegatives;
            return this;
        }
        
        /**
         * Set the number of false negatives.
         * 
         * @param falseNegatives the false negatives count
         * @return this builder for method chaining
         */
        public Builder falseNegatives(int falseNegatives) {
            this.falseNegatives = falseNegatives;
            return this;
        }
        
        /**
         * Build the ModelEvaluationResult object.
         * 
         * @return a new ModelEvaluationResult with the builder's properties
         * @throws IllegalStateException if required fields are missing
         */
        public ModelEvaluationResult build() {
            return new ModelEvaluationResult(
                accuracy, precision, recall, f1Score, auc, confusionMatrix,
                truePositives, falsePositives, trueNegatives, falseNegatives
            );
        }
    }
}
