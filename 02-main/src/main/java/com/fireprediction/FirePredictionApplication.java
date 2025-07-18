package com.fireprediction;

import com.fireprediction.database.Database;
import com.fireprediction.database.DatabaseException;
import com.fireprediction.database.SupabaseDatabase;
import com.fireprediction.ml.FireRiskPredictor;
import com.fireprediction.ml.PredictorException;
import com.fireprediction.ml.PredictorFactory;
import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import com.fireprediction.notification.EmailService;
import com.fireprediction.notification.FireRiskNotificationHandler;
import com.fireprediction.sensor.DangerousSimulatedSensor;
import com.fireprediction.sensor.ESP32DHT22Sensor;
import com.fireprediction.sensor.SensorException;
import com.fireprediction.sensor.SensorManager;
import com.fireprediction.sensor.SimulatedSensor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main application class for the Fire Prediction System.
 * 
 * OOP Principles:
 * - Facade Pattern: Provides a simplified interface to the subsystems
 * - Composition: Composed of multiple subsystems (sensor, database, ML)
 * - Dependency Inversion: Depends on abstractions, not concrete implementations
 */
public class FirePredictionApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(FirePredictionApplication.class);
    
    // Environment variable names
    private static final String ENV_APP_NAME = "APP_NAME";
    private static final String ENV_SENSOR_READ_INTERVAL = "SENSOR_READ_INTERVAL_SECONDS";
    private static final String ENV_DEFAULT_PREDICTOR = "DEFAULT_PREDICTOR";
    private static final String ENV_SIMULATION_MODE = "SIMULATION_MODE_ENABLED";
    
    // Default values
    private static final String DEFAULT_APP_NAME = "Fire Prediction System";
    private static final int DEFAULT_READ_INTERVAL = 60; // 1 minute
    private static final String DEFAULT_PREDICTOR_TYPE = "SMILE";
    private static final boolean DEFAULT_SIMULATION_MODE = true;
    
    // Application components
    private SensorManager sensorManager;
    private Database database;
    private FireRiskPredictor predictor;
    private String appName;
    private boolean simulationMode;
    private FireRiskNotificationHandler notificationHandler;
    
    // UI components
    private TableView<SensorReading> readingsTable;
    private ObservableList<SensorReading> readings = FXCollections.observableArrayList();
    private Map<String, XYChart.Series<Number, Number>> temperatureSeries = new HashMap<>();
    private Map<String, XYChart.Series<Number, Number>> humiditySeries = new HashMap<>();
    private Label statusLabel;
    private Label riskLabel;
    private ScheduledExecutorService uiUpdateScheduler;
    
    /**
     * Application entry point.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * JavaFX start method.
     * 
     * @param primaryStage the primary stage for the application
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Fire Prediction Application");
        
        try {
            // Initialize configuration
            initializeConfig();
            
            // Initialize components
            initializeComponents();
            
            // Create the UI
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));
            
            // Top header
            HBox header = createHeader();
            root.setTop(header);
            
            // Tab pane for different views
            TabPane tabPane = new TabPane();
            
            // Dashboard tab
            Tab dashboardTab = new Tab("Dashboard");
            dashboardTab.setContent(createDashboardView());
            dashboardTab.setClosable(false);
            
            // Readings tab
            Tab readingsTab = new Tab("Sensor Readings");
            readingsTab.setContent(createReadingsView());
            readingsTab.setClosable(false);
            
            // Risk levels tab
            Tab riskLevelsTab = new Tab("Risk Levels");
            riskLevelsTab.setContent(createRiskLevelsView());
            riskLevelsTab.setClosable(false);
            
            tabPane.getTabs().addAll(dashboardTab, readingsTab, riskLevelsTab);
            root.setCenter(tabPane);
            
            Scene scene = new Scene(root, 900, 700);
            
            // Load minimal CSS to avoid StackOverflowError
            try {
                // Try to load minimal CSS from classpath
                URL cssResource = getClass().getResource("/styles/minimal.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    logger.info("Minimal CSS loaded successfully from: " + cssResource.toExternalForm());
                } else {
                    // Fallback to file system
                    String cssPath = "src/main/resources/styles/minimal.css";
                    File cssFile = new File(cssPath);
                    if (cssFile.exists()) {
                        scene.getStylesheets().add(cssFile.toURI().toString());
                        logger.info("Minimal CSS loaded successfully from: " + cssFile.toURI().toString());
                    } else {
                        logger.warn("Could not find minimal CSS file");
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading CSS: " + e.getMessage(), e);
            }
            
            primaryStage.setTitle(appName);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Start sensor readings
            sensorManager.startScheduledReadings();
            
            // Start UI updates
            startUIUpdates();
            
            // Load or train the model in the background
            CompletableFuture.runAsync(() -> {
                try {
                    // Try to load the model
                    byte[] modelData = database.loadModel(predictor.getAlgorithmName());
                    if (modelData != null && modelData.length > 0) {
                        logger.info("Loading existing model");
                        predictor.loadModel(modelData);
                    } else {
                        // Train a new model if no existing model was found
                        logger.info("Training new model");
                        trainModel();
                    }
                } catch (Exception e) {
                    logger.warn("Failed to load model, training new one: {}", e.getMessage());
                    try {
                        trainModel();
                    } catch (Exception ex) {
                        logger.error("Failed to train model: {}", ex.getMessage(), ex);
                    }
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to start application: {}", e.getMessage(), e);
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }
    
    /**
     * Create the header panel.
     * 
     * @return the header panel
     */
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        Label titleLabel = new Label(appName);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        statusLabel = new Label("Status: " + (simulationMode ? "SIMULATION" : "REAL") + " mode");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        riskLabel = new Label("Current Risk: Unknown");
        riskLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        header.getChildren().addAll(titleLabel, statusLabel, riskLabel);
        
        return header;
    }
    
    /**
     * Create the dashboard view.
     * 
     * @return the dashboard panel
     */
    private GridPane createDashboardView() {
        GridPane dashboard = new GridPane();
        dashboard.setPadding(new Insets(10));
        dashboard.setHgap(10);
        dashboard.setVgap(10);
        
        // Temperature chart
        NumberAxis tempXAxis = new NumberAxis();
        NumberAxis tempYAxis = new NumberAxis();
        tempXAxis.setLabel("Time (seconds)");
        tempYAxis.setLabel("Temperature (°C)");
        
        LineChart<Number, Number> tempChart = new LineChart<>(tempXAxis, tempYAxis);
        tempChart.setTitle("Temperature Readings");
        tempChart.setAnimated(false);
        
        // Humidity chart
        NumberAxis humXAxis = new NumberAxis();
        NumberAxis humYAxis = new NumberAxis();
        humXAxis.setLabel("Time (seconds)");
        humYAxis.setLabel("Humidity (%)");
        
        LineChart<Number, Number> humChart = new LineChart<>(humXAxis, humYAxis);
        humChart.setTitle("Humidity Readings");
        humChart.setAnimated(false);
        
        // Add charts to dashboard
        dashboard.add(tempChart, 0, 0);
        dashboard.add(humChart, 1, 0);
        
        // Latest readings panel
        VBox latestReadings = new VBox(10);
        latestReadings.setPadding(new Insets(10));
        latestReadings.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        Label latestHeader = new Label("Latest Readings");
        latestHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        TableView<SensorReading> latestTable = new TableView<>();
        latestTable.setMaxHeight(200);
        
        TableColumn<SensorReading, String> sensorCol = new TableColumn<>("Sensor");
        sensorCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSensorId()));
        
        TableColumn<SensorReading, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));
        
        TableColumn<SensorReading, String> tempCol = new TableColumn<>("Temp (°C)");
        tempCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getTemperature())));
        
        TableColumn<SensorReading, String> humCol = new TableColumn<>("Humidity (%)");
        humCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getHumidity())));
        
        TableColumn<SensorReading, String> riskCol = new TableColumn<>("Risk Level");
        riskCol.setCellValueFactory(cellData -> {
            FireRiskLevel level = cellData.getValue().getRiskLevel();
            if (level == null) {
                level = FireRiskLevel.fromTemperature(cellData.getValue().getTemperature());
            }
            return new javafx.beans.property.SimpleStringProperty(level.name());
        });
        
        latestTable.getColumns().addAll(sensorCol, locationCol, tempCol, humCol, riskCol);
        latestTable.setItems(readings);
        
        latestReadings.getChildren().addAll(latestHeader, latestTable);
        
        dashboard.add(latestReadings, 0, 1, 2, 1);
        
        return dashboard;
    }
    
    /**
     * Create the readings view with full table of readings.
     * 
     * @return the readings panel
     */
    private VBox createReadingsView() {
        VBox readingsView = new VBox(10);
        readingsView.setPadding(new Insets(10));
        
        Label title = new Label("All Sensor Readings");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        readingsTable = new TableView<>();
        
        TableColumn<SensorReading, String> idCol = new TableColumn<>("Sensor ID");
        idCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSensorId()));
        
        TableColumn<SensorReading, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));
        
        TableColumn<SensorReading, String> tempCol = new TableColumn<>("Temperature (°C)");
        tempCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getTemperature())));
        
        TableColumn<SensorReading, String> humCol = new TableColumn<>("Humidity (%)");
        humCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getHumidity())));
        
        TableColumn<SensorReading, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTimestamp().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        TableColumn<SensorReading, String> riskLevelCol = new TableColumn<>("Risk Level");
        riskLevelCol.setCellValueFactory(cellData -> {
            FireRiskLevel level = cellData.getValue().getRiskLevel();
            if (level == null) {
                level = FireRiskLevel.fromTemperature(cellData.getValue().getTemperature());
            }
            return new javafx.beans.property.SimpleStringProperty(level.name());
        });
        
        TableColumn<SensorReading, String> riskProbCol = new TableColumn<>("Risk Probability");
        riskProbCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f", cellData.getValue().getRiskProbability())));
        
        readingsTable.getColumns().addAll(idCol, locationCol, tempCol, humCol, timeCol, riskLevelCol, riskProbCol);
        readingsTable.setItems(readings);
        
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(5));
        
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(e -> refreshReadings());
        
        Button clearButton = new Button("Clear Table");
        clearButton.setOnAction(e -> readings.clear());
        
        controls.getChildren().addAll(refreshButton, clearButton);
        
        readingsView.getChildren().addAll(title, readingsTable, controls);
        
        return readingsView;
    }
    
    /**
     * Create the risk levels information view.
     * 
     * @return the risk levels panel
     */
    private VBox createRiskLevelsView() {
        VBox riskView = new VBox(15);
        riskView.setPadding(new Insets(20));
        
        Label title = new Label("Fire Risk Levels");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        GridPane riskGrid = new GridPane();
        riskGrid.setHgap(20);
        riskGrid.setVgap(15);
        riskGrid.setPadding(new Insets(10));
        
        // Headers
        Label levelHeader = new Label("Risk Level");
        levelHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label tempHeader = new Label("Temperature Range");
        tempHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label descHeader = new Label("Description");
        descHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label actionHeader = new Label("Recommended Action");
        actionHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        riskGrid.add(levelHeader, 0, 0);
        riskGrid.add(tempHeader, 1, 0);
        riskGrid.add(descHeader, 2, 0);
        riskGrid.add(actionHeader, 3, 0);
        
        // Risk level rows
        int row = 1;
        for (FireRiskLevel level : FireRiskLevel.values()) {
            Label levelLabel = new Label(level.name());
            levelLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            levelLabel.setTextFill(level.getDisplayColor());
            
            Label tempLabel = new Label(level.getMinTemperature() + "°C - " + level.getMaxTemperature() + "°C");
            Label descLabel = new Label(level.getDescription());
            Label actionLabel = new Label(level.getRecommendedAction());
            
            riskGrid.add(levelLabel, 0, row);
            riskGrid.add(tempLabel, 1, row);
            riskGrid.add(descLabel, 2, row);
            riskGrid.add(actionLabel, 3, row);
            
            row++;
        }
        
        // Add information about the prediction system
        Label predictionInfo = new Label("Prediction System Information");
        predictionInfo.setFont(Font.font("System", FontWeight.BOLD, 16));
        predictionInfo.setPadding(new Insets(20, 0, 10, 0));
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10));
        
        Label predictorLabel = new Label("Current Predictor:");
        predictorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label predictorValueLabel = new Label(predictor.getName() + " (" + predictor.getAlgorithmName() + ")");
        
        Label modeLabel = new Label("Operating Mode:");
        modeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label modeValueLabel = new Label(simulationMode ? "Simulation" : "Real Hardware");
        
        infoGrid.add(predictorLabel, 0, 0);
        infoGrid.add(predictorValueLabel, 1, 0);
        infoGrid.add(modeLabel, 0, 1);
        infoGrid.add(modeValueLabel, 1, 1);
        
        riskView.getChildren().addAll(title, riskGrid, predictionInfo, infoGrid);
        
        return riskView;
    }
    
    /**
     * Start periodic UI updates.
     */
    private void startUIUpdates() {
        // Add listener to sensor manager
        sensorManager.addReadingListener(this::processSensorReading);
        
        // Schedule UI refresh
        uiUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        uiUpdateScheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                refreshReadings();
            });
        }, 0, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Process a new sensor reading.
     * 
     * @param reading the new sensor reading
     */
    private void processSensorReading(SensorReading reading) {
        Platform.runLater(() -> {
            // Add to readings list
            readings.add(0, reading); // Add at beginning
            
            // Limit size
            if (readings.size() > 100) {
                readings.remove(100, readings.size());
            }
            
            // Update charts
            updateCharts(reading);
            
            // Update risk level
            FireRiskLevel riskLevel = reading.getRiskLevel();
            if (riskLevel == null) {
                riskLevel = FireRiskLevel.fromTemperature(reading.getTemperature());
            }
            
            riskLabel.setText("Current Risk: " + riskLevel.name());
            riskLabel.setTextFill(riskLevel.getDisplayColor());
        });
    }
    
    /**
     * Update charts with new sensor reading.
     * 
     * @param reading the new sensor reading
     */
    private void updateCharts(SensorReading reading) {
        String sensorId = reading.getSensorId();
        
        // Get or create temperature series for this sensor
        XYChart.Series<Number, Number> tempSeries = temperatureSeries.get(sensorId);
        if (tempSeries == null) {
            tempSeries = new XYChart.Series<>();
            tempSeries.setName(sensorId + " (" + reading.getLocation() + ")");
            temperatureSeries.put(sensorId, tempSeries);
            
            // Add to all temperature charts
            for (Tab tab : ((TabPane) ((BorderPane) ((Scene) riskLabel.getScene()).getRoot()).getCenter()).getTabs()) {
                if (tab.getText().equals("Dashboard")) {
                    GridPane dashboard = (GridPane) tab.getContent();
                    LineChart<Number, Number> chart = (LineChart<Number, Number>) dashboard.getChildren().get(0);
                    if (!chart.getData().contains(tempSeries)) {
                        chart.getData().add(tempSeries);
                    }
                }
            }
        }
        
        // Get or create humidity series for this sensor
        XYChart.Series<Number, Number> humSeries = humiditySeries.get(sensorId);
        if (humSeries == null) {
            humSeries = new XYChart.Series<>();
            humSeries.setName(sensorId + " (" + reading.getLocation() + ")");
            humiditySeries.put(sensorId, humSeries);
            
            // Add to all humidity charts
            for (Tab tab : ((TabPane) ((BorderPane) ((Scene) riskLabel.getScene()).getRoot()).getCenter()).getTabs()) {
                if (tab.getText().equals("Dashboard")) {
                    GridPane dashboard = (GridPane) tab.getContent();
                    LineChart<Number, Number> chart = (LineChart<Number, Number>) dashboard.getChildren().get(1);
                    if (!chart.getData().contains(humSeries)) {
                        chart.getData().add(humSeries);
                    }
                }
            }
        }
        
        // Add data points (using time in seconds since start)
        long timePoint = System.currentTimeMillis() / 1000;
        tempSeries.getData().add(new XYChart.Data<>(timePoint, reading.getTemperature()));
        humSeries.getData().add(new XYChart.Data<>(timePoint, reading.getHumidity()));
        
        // Limit number of points to prevent memory issues
        if (tempSeries.getData().size() > 50) {
            tempSeries.getData().remove(0);
        }
        
        if (humSeries.getData().size() > 50) {
            humSeries.getData().remove(0);
        }
    }
    
    /**
     * Refresh readings from database.
     */
    private void refreshReadings() {
        try {
            List<SensorReading> dbReadings = database.getAllSensorReadings();
            
            Platform.runLater(() -> {
                readings.clear();
                readings.addAll(dbReadings);
                
                // Update status
                statusLabel.setText("Status: Connected | Last update: " + 
                        java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            });
        } catch (Exception e) {
            logger.error("Error refreshing readings: {}", e.getMessage(), e);
            
            Platform.runLater(() -> {
                statusLabel.setText("Status: Error | " + e.getMessage());
            });
        }
    }
    
    /**
     * Initialize configuration from environment variables.
     */
    private void initializeConfig() {
        // Read environment variables
        appName = getEnvOrDefault(ENV_APP_NAME, DEFAULT_APP_NAME);
        simulationMode = Boolean.parseBoolean(getEnvOrDefault(ENV_SIMULATION_MODE, String.valueOf(DEFAULT_SIMULATION_MODE)));
        
        logger.info("Application name: {}", appName);
        logger.info("Simulation mode: {}", simulationMode ? "ENABLED" : "DISABLED");
    }
    
    /**
     * Initialize application components.
     * 
     * @throws DatabaseException if database initialization fails
     * @throws SensorException if sensor initialization fails
     * @throws PredictorException if predictor initialization fails
     */
    private void initializeComponents() throws DatabaseException, SensorException, PredictorException {
        // Initialize database
        logger.info("Initializing database");
        database = new SupabaseDatabase();
        database.initialize();
        
        // Initialize sensor manager
        logger.info("Initializing sensor manager");
        int readInterval = Integer.parseInt(
                getEnvOrDefault(ENV_SENSOR_READ_INTERVAL, String.valueOf(DEFAULT_READ_INTERVAL)));
        sensorManager = new SensorManager(readInterval);
        
        // Add sensors
        if (simulationMode) {
            logger.info("Adding simulated sensors");
            sensorManager.addSensor(new SimulatedSensor("SIM1", "Living Room"));
            sensorManager.addSensor(new SimulatedSensor("SIM2", "Kitchen"));
            // Replace one normal sensor with a dangerous one to trigger alerts
            sensorManager.addSensor(new DangerousSimulatedSensor("DANGER-SIM", "Server Room", true)); // true = EXTREME risk (51-60°C)
        } else {
            logger.info("Adding real sensors");
            // In a real system, these would be configured based on available hardware
            sensorManager.addSensor(new ESP32DHT22Sensor("ESP1", "COM3", "Living Room"));
        }
        
        // Initialize sensors
        sensorManager.initializeSensors();
        
        // Initialize email notification service
        logger.info("Initializing email notification service");
        EmailService emailService = new EmailService(
                "smtp.gmail.com",                  // Email host
                "587",                             // Port
                "mustafa.khafaga@gmail.com",       // Sender email
                "pzzz xwis ygmu lyfm",             // App password 
                "briancondon894@gmail.com"         // Recipient email
        );
        
        // Create and register notification handler
        notificationHandler = new FireRiskNotificationHandler(emailService);
        sensorManager.addReadingListener(notificationHandler);
        logger.info("Email notification handler registered with sensor manager");
        
        // Add reading listener to save readings to database
        sensorManager.addReadingListener(reading -> {
            try {
                // Make prediction
                if (predictor != null && predictor.isTrained()) {
                    double probability = predictor.predictProbability(reading);
                    reading.setRiskProbability(probability);
                    reading.setRiskLevel(predictor.predictRiskLevel(reading));
                }
                
                // Save to database
                database.saveSensorReading(reading);
            } catch (Exception e) {
                logger.error("Error processing sensor reading: {}", e.getMessage(), e);
            }
        });
        
        // Initialize predictor
        logger.info("Initializing predictor");
        String predictorType = getEnvOrDefault(ENV_DEFAULT_PREDICTOR, DEFAULT_PREDICTOR_TYPE);
        predictor = PredictorFactory.createAndInitialize(predictorType);
    }
    
    /**
     * Train the model using historical data.
     * 
     * @throws DatabaseException if database access fails
     * @throws PredictorException if training fails
     */
    private void trainModel() throws DatabaseException, PredictorException {
        logger.info("Training model with historical data");
        
        // Get historical data
        List<SensorReading> historicalData = database.getAllSensorReadings();
        
        if (historicalData.isEmpty()) {
            logger.warn("No historical data available for training");
            return;
        }
        
        logger.info("Training with {} readings", historicalData.size());
        
        // Train the model
        predictor.trainModel(historicalData);
        
        // Save the model
        logger.info("Saving trained model");
        byte[] modelData = predictor.saveModel();
        database.saveModel(modelData, predictor.getAlgorithmName());
    }
    
    /**
     * Show error dialog and exit application.
     * 
     * @param message error message to display
     */
    private void showErrorAndExit(String message) {
        logger.error(message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Fatal Error");
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });
    }
    
    /**
     * Get environment variable with default value.
     * 
     * @param name environment variable name
     * @param defaultValue default value if variable is not set
     * @return environment variable value or default
     */
    private String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
    
    /**
     * JavaFX stop method.
     */
    @Override
    public void stop() {
        logger.info("Stopping Fire Prediction Application");
        
        // Stop UI updates
        if (uiUpdateScheduler != null) {
            uiUpdateScheduler.shutdown();
            try {
                if (!uiUpdateScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    uiUpdateScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                uiUpdateScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Close resources
        if (sensorManager != null) {
            sensorManager.close();
        }
        
        if (database != null) {
            try {
                database.close();
            } catch (DatabaseException e) {
                logger.error("Error closing database: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Application stopped");
    }
}
