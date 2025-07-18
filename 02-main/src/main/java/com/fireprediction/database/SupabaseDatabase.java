package com.fireprediction.database;

import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Implementation of the Database interface using Supabase as the backend.
 * 
 * OOP Principles:
 * - Polymorphism: Implements Database interface
 * - Encapsulation: Hides Supabase API details
 * - Single Responsibility: Handles only database operations
 */
public class SupabaseDatabase implements Database {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseDatabase.class);
    
    private static final String ENV_SUPABASE_URL = "SUPABASE_URL";
    private static final String ENV_SUPABASE_KEY = "SUPABASE_KEY";
    private static final String TABLE_SENSOR_READINGS = "sensor_readings";
    private static final String TABLE_MODELS = "ml_models";
    
    private final String supabaseUrl;
    private final String supabaseKey;
    private boolean connected = false;
    private CloseableHttpClient httpClient;
    
    /**
     * Create a SupabaseDatabase with environment variables.
     * 
     * @throws DatabaseException if environment variables are missing
     */
    public SupabaseDatabase() throws DatabaseException {
        String url = System.getenv(ENV_SUPABASE_URL);
        String key = System.getenv(ENV_SUPABASE_KEY);
        
        if (url == null || url.isEmpty()) {
            throw new DatabaseException("Environment variable " + ENV_SUPABASE_URL + " not set");
        }
        
        if (key == null || key.isEmpty()) {
            throw new DatabaseException("Environment variable " + ENV_SUPABASE_KEY + " not set");
        }
        
        this.supabaseUrl = url;
        this.supabaseKey = key;
        logger.info("SupabaseDatabase initialized with URL: {}", url);
    }
    
    /**
     * Create a SupabaseDatabase with explicit credentials.
     * 
     * @param supabaseUrl Supabase project URL
     * @param supabaseKey Supabase API key
     */
    public SupabaseDatabase(String supabaseUrl, String supabaseKey) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        logger.info("SupabaseDatabase initialized with URL: {}", supabaseUrl);
    }
    
    /**
     * Initialize the database connection.
     * 
     * @throws DatabaseException if initialization fails
     */
    @Override
    public void initialize() throws DatabaseException {
        logger.info("Initializing Supabase database connection");
        
        try {
            // Create HTTP client
            httpClient = HttpClients.createDefault();
            
            // Test connection with a simple query
            HttpGet request = new HttpGet(supabaseUrl + "/rest/v1/" + TABLE_SENSOR_READINGS + "?limit=1");
            request.addHeader("apikey", supabaseKey);
            request.addHeader("Authorization", "Bearer " + supabaseKey);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    connected = true;
                    logger.info("Successfully connected to Supabase");
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new DatabaseException("Failed to connect to Supabase: " + 
                            response.getStatusLine() + ", " + responseBody);
                }
            }
        } catch (Exception e) {
            connected = false;
            throw new DatabaseException("Error initializing Supabase connection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save a sensor reading to the database.
     * 
     * @param reading the sensor reading to save
     * @return true if saved successfully
     * @throws DatabaseException if save fails
     */
    @Override
    public boolean saveSensorReading(SensorReading reading) throws DatabaseException {
        if (!connected) {
            throw new DatabaseException("Database not connected");
        }
        
        logger.debug("Saving sensor reading: {}", reading);
        
        try {
            // Create JSON payload
            JSONObject json = new JSONObject();
            json.put("sensor_id", reading.getSensorId());
            json.put("temperature", reading.getTemperature());
            json.put("humidity", reading.getHumidity());
            json.put("location", reading.getLocation());
            json.put("timestamp", reading.getTimestamp().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
            
            if (reading.getRiskLevel() != null) {
                json.put("risk_level", reading.getRiskLevel().name());
                json.put("risk_probability", reading.getRiskProbability());
            }
            
            // Create POST request
            HttpPost request = new HttpPost(supabaseUrl + "/rest/v1/" + TABLE_SENSOR_READINGS);
            request.addHeader("apikey", supabaseKey);
            request.addHeader("Authorization", "Bearer " + supabaseKey);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Prefer", "return=minimal");
            
            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            request.setEntity(entity);
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.debug("Successfully saved sensor reading");
                    return true;
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    logger.error("Failed to save sensor reading: {} - {}", 
                            response.getStatusLine(), responseBody);
                    return false;
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Error saving sensor reading: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all sensor readings from the database.
     * 
     * @return list of sensor readings
     * @throws DatabaseException if retrieval fails
     */
    @Override
    public List<SensorReading> getAllSensorReadings() throws DatabaseException {
        return getSensorReadings(null, null);
    }
    
    /**
     * Get sensor readings from a specific time period.
     * 
     * @param startTime start of the time period (or null for no start constraint)
     * @param endTime end of the time period (or null for no end constraint)
     * @return list of sensor readings within the time period
     * @throws DatabaseException if retrieval fails
     */
    @Override
    public List<SensorReading> getSensorReadings(LocalDateTime startTime, LocalDateTime endTime) 
            throws DatabaseException {
        if (!connected) {
            throw new DatabaseException("Database not connected");
        }
        
        logger.debug("Getting sensor readings from {} to {}", startTime, endTime);
        
        try {
            StringBuilder url = new StringBuilder(supabaseUrl + "/rest/v1/" + TABLE_SENSOR_READINGS);
            
            // Build query parameters
            boolean hasQuery = false;
            if (startTime != null) {
                url.append("?timestamp=gte.")
                   .append(startTime.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
                hasQuery = true;
            }
            
            if (endTime != null) {
                url.append(hasQuery ? "&" : "?")
                   .append("timestamp=lte.")
                   .append(endTime.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
            }
            
            // Create GET request
            HttpGet request = new HttpGet(url.toString());
            request.addHeader("apikey", supabaseKey);
            request.addHeader("Authorization", "Bearer " + supabaseKey);
            request.addHeader("Content-Type", "application/json");
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return parseSensorReadings(responseBody);
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new DatabaseException("Failed to get sensor readings: " + 
                            response.getStatusLine() + ", " + responseBody);
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Error getting sensor readings: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get readings from a specific sensor.
     * 
     * @param sensorId ID of the sensor
     * @return list of readings from the specified sensor
     * @throws DatabaseException if retrieval fails
     */
    @Override
    public List<SensorReading> getReadingsForSensor(String sensorId) throws DatabaseException {
        if (!connected) {
            throw new DatabaseException("Database not connected");
        }
        
        logger.debug("Getting readings for sensor {}", sensorId);
        
        try {
            String url = supabaseUrl + "/rest/v1/" + TABLE_SENSOR_READINGS + 
                    "?sensor_id=eq." + sensorId + "&order=timestamp.desc";
            
            // Create GET request
            HttpGet request = new HttpGet(url);
            request.addHeader("apikey", supabaseKey);
            request.addHeader("Authorization", "Bearer " + supabaseKey);
            request.addHeader("Content-Type", "application/json");
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return parseSensorReadings(responseBody);
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new DatabaseException("Failed to get sensor readings: " + 
                            response.getStatusLine() + ", " + responseBody);
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Error getting readings for sensor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save the trained model to the database.
     * 
     * @param modelData serialized model data
     * @param modelName name of the model
     * @return true if saved successfully
     * @throws DatabaseException if save fails
     */
    @Override
    public boolean saveModel(byte[] modelData, String modelName) throws DatabaseException {
        if (!connected) {
            throw new DatabaseException("Database not connected");
        }
        
        logger.info("Saving ML model: {}", modelName);
        
        try {
            // Encode model data as Base64 string
            String encodedModel = Base64.getEncoder().encodeToString(modelData);
            
            // Create JSON payload
            JSONObject json = new JSONObject();
            json.put("name", modelName);
            json.put("model_data", encodedModel);
            json.put("created_at", LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
            
            // Check if model already exists (upsert if it does)
            String url = supabaseUrl + "/rest/v1/" + TABLE_MODELS;
            String preferHeader = "return=minimal";
            
            HttpGet checkRequest = new HttpGet(url + "?name=eq." + modelName);
            checkRequest.addHeader("apikey", supabaseKey);
            checkRequest.addHeader("Authorization", "Bearer " + supabaseKey);
            
            try (CloseableHttpResponse checkResponse = httpClient.execute(checkRequest)) {
                String responseBody = EntityUtils.toString(checkResponse.getEntity());
                JSONArray results = new JSONArray(responseBody);
                
                if (results.length() > 0) {
                    // Model exists, use upsert
                    preferHeader += ", resolution=merge-duplicates";
                }
            }
            
            // Create POST request for upsert
            HttpPost request = new HttpPost(url);
            request.addHeader("apikey", supabaseKey);
            request.addHeader("Authorization", "Bearer " + supabaseKey);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Prefer", preferHeader);
            
            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            request.setEntity(entity);
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Successfully saved ML model");
                    return true;
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    logger.error("Failed to save ML model: {} - {}", 
                            response.getStatusLine(), responseBody);
                    return false;
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Error saving ML model: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load a trained model from the database.
     * 
     * @param modelName name of the model to load
     * @return serialized model data
     * @throws DatabaseException if load fails
     */
    @Override
    public byte[] loadModel(String modelName) throws DatabaseException {
        if (!connected) {
            throw new DatabaseException("Database not connected");
        }
        
        logger.info("Loading ML model: {}", modelName);
        
        try {
            String url = supabaseUrl + "/rest/v1/" + TABLE_MODELS + 
                    "?name=eq." + modelName + "&order=created_at.desc&limit=1";
            
            // Create GET request
            HttpGet request = new HttpGet(url);
            request.addHeader("apikey", supabaseKey);
            request.addHeader("Authorization", "Bearer " + supabaseKey);
            request.addHeader("Content-Type", "application/json");
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONArray results = new JSONArray(responseBody);
                    
                    if (results.length() == 0) {
                        throw new DatabaseException("Model not found: " + modelName);
                    }
                    
                    JSONObject model = results.getJSONObject(0);
                    String encodedData = model.getString("model_data");
                    return Base64.getDecoder().decode(encodedData);
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    throw new DatabaseException("Failed to load ML model: " + 
                            response.getStatusLine() + ", " + responseBody);
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Error loading ML model: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if the database connection is active.
     * 
     * @return true if connected
     */
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Close the database connection.
     * 
     * @throws DatabaseException if closing fails
     */
    @Override
    public void close() throws DatabaseException {
        logger.info("Closing Supabase database connection");
        
        if (httpClient != null) {
            try {
                httpClient.close();
                connected = false;
            } catch (Exception e) {
                throw new DatabaseException("Error closing database connection: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Parse JSON response into a list of SensorReading objects.
     * 
     * @param json JSON array string from Supabase
     * @return list of sensor readings
     */
    private List<SensorReading> parseSensorReadings(String json) {
        List<SensorReading> readings = new ArrayList<>();
        
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            
            String sensorId = obj.getString("sensor_id");
            double temperature = obj.getDouble("temperature");
            double humidity = obj.getDouble("humidity");
            String location = obj.getString("location");
            
            // Parse timestamp
            String timestampStr = obj.getString("timestamp");
            LocalDateTime timestamp = LocalDateTime.parse(
                    timestampStr.replace("Z", ""), 
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC));
            
            // Create reading using builder
            SensorReading.Builder builder = new SensorReading.Builder()
                    .sensorId(sensorId)
                    .temperature(temperature)
                    .humidity(humidity)
                    .location(location)
                    .timestamp(timestamp);
            
            // Add risk information if available
            if (obj.has("risk_level") && !obj.isNull("risk_level")) {
                String riskLevelStr = obj.getString("risk_level");
                FireRiskLevel riskLevel = FireRiskLevel.valueOf(riskLevelStr);
                builder.riskLevel(riskLevel);
                
                if (obj.has("risk_probability") && !obj.isNull("risk_probability")) {
                    double probability = obj.getDouble("risk_probability");
                    builder.riskProbability(probability);
                }
            }
            
            readings.add(builder.build());
        }
        
        return readings;
    }
}
