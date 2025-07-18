package com.fireprediction.ui;

import com.fireprediction.database.Database;
import com.fireprediction.ml.FireRiskPredictor;
import com.fireprediction.ml.ModelEvaluationResult;
import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import com.fireprediction.sensor.SensorManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JavaFX user interface for the Fire Prediction System.
 */
public class FirePredictionUI {

    private static final Logger logger = LoggerFactory.getLogger(FirePredictionUI.class);
    
    private final SensorManager sensorManager;
    private final Database database;
    private final FireRiskPredictor predictor;
    
    // UI components
    private Stage stage;
    private TableView<SensorReading> readingsTable;
    private ObservableList<SensorReading> readings = FXCollections.observableArrayList();
    private LineChart<Number, Number> temperatureChart;
    private LineChart<Number, Number> humidityChart;
    private Label statusLabel;
    private Label riskLabel;
    private Label accuracyLabel;
    
    // Chart data
    private XYChart.Series<Number, Number> tempSeries;
    private XYChart.Series<Number, Number> humiditySeries;
    
    // Update scheduler
    private ScheduledExecutorService scheduler;
    
    /**
     * Create a new FirePredictionUI.
     * 
     * @param sensorManager the sensor manager
     * @param database the database
     * @param predictor the predictor
     */
    public FirePredictionUI(SensorManager sensorManager, Database database, FireRiskPredictor predictor) {
        this.sensorManager = sensorManager;
        this.database = database;
        this.predictor = predictor;
    }
    
    /**
     * Start the UI.
     * 
     * @param primaryStage the primary stage
     */
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        
        // Create UI components
        createUI();
        
        // Start data updates
        startDataUpdates();
        
        // Show the stage
        stage.setTitle("Fire Prediction System");
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
    }
    
    /**
     * Create the user interface.
     */
    private void createUI() {
        // Create root layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create top panel with status
        HBox topPanel = createTopPanel();
        root.setTop(topPanel);
        
        // Create center panel with charts
        TabPane tabPane = new TabPane();
        
        // Dashboard tab
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(createDashboardPanel());
        
        // Readings tab
        Tab readingsTab = new Tab("Sensor Readings");
        readingsTab.setClosable(false);
        readingsTab.setContent(createReadingsPanel());
        
        // Add tabs
        tabPane.getTabs().addAll(dashboardTab, readingsTab);
        
        root.setCenter(tabPane);
        
        // Create scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    /**
     * Create the top panel with status information.
     * 
     * @return the top panel
     */
    private HBox createTopPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        
        // Status label
        statusLabel = new Label("Status: Initializing...");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Risk label
        riskLabel = new Label("Current Risk: Unknown");
        riskLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Model accuracy label
        accuracyLabel = new Label("Model Accuracy: Unknown");
        accuracyLabel.setFont(Font.font("System", 12));
        
        panel.getChildren().addAll(statusLabel, riskLabel, accuracyLabel);
        
        return panel;
    }
    
    /**
     * Create the dashboard panel with charts.
     * 
     * @return the dashboard panel
     */
    private GridPane createDashboardPanel() {
        GridPane panel = new GridPane();
        panel.setPadding(new Insets(10));
        panel.setHgap(10);
        panel.setVgap(10);
        
        // Create temperature chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (minutes)");
        yAxis.setLabel("Temperature (°C)");
        
        temperatureChart = new LineChart<>(xAxis, yAxis);
        temperatureChart.setTitle("Temperature History");
        temperatureChart.setAnimated(false);
        
        tempSeries = new XYChart.Series<>();
        tempSeries.setName("Temperature");
        temperatureChart.getData().add(tempSeries);
        
        // Create humidity chart
        NumberAxis xAxis2 = new NumberAxis();
        NumberAxis yAxis2 = new NumberAxis();
        xAxis2.setLabel("Time (minutes)");
        yAxis2.setLabel("Humidity (%)");
        
        humidityChart = new LineChart<>(xAxis2, yAxis2);
        humidityChart.setTitle("Humidity History");
        humidityChart.setAnimated(false);
        
        humiditySeries = new XYChart.Series<>();
        humiditySeries.setName("Humidity");
        humidityChart.getData().add(humiditySeries);
        
        // Add charts to panel
        panel.add(temperatureChart, 0, 0);
        panel.add(humidityChart, 1, 0);
        
        // Prediction panel
        VBox predictionPanel = createPredictionPanel();
        panel.add(predictionPanel, 0, 1, 2, 1);
        
        return panel;
    }
    
    /**
     * Create the prediction panel with risk levels.
     * 
     * @return the prediction panel
     */
    private VBox createPredictionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        // Title
        Label title = new Label("Fire Risk Levels");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Risk levels
        GridPane riskGrid = new GridPane();
        riskGrid.setHgap(10);
        riskGrid.setVgap(5);
        
        int row = 0;
        for (FireRiskLevel level : FireRiskLevel.values()) {
            Label nameLabel = new Label(level.name());
            nameLabel.setTextFill(level.getDisplayColor());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            Label descLabel = new Label(level.getDescription());
            Label actionLabel = new Label(level.getRecommendedAction());
            
            riskGrid.add(nameLabel, 0, row);
            riskGrid.add(descLabel, 1, row);
            riskGrid.add(actionLabel, 2, row);
            
            row++;
        }
        
        // Add to panel
        panel.getChildren().addAll(title, riskGrid);
        
        return panel;
    }
    
    /**
     * Create the readings panel with table.
     * 
     * @return the readings panel
     */
    private VBox createReadingsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        // Create table
        readingsTable = new TableView<>();
        
        // Columns
        TableColumn<SensorReading, String> sensorIdCol = new TableColumn<>("Sensor ID");
        sensorIdCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSensorId()));
        
        TableColumn<SensorReading, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));
        
        TableColumn<SensorReading, String> tempCol = new TableColumn<>("Temperature (°C)");
        tempCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getTemperature())));
        
        TableColumn<SensorReading, String> humidityCol = new TableColumn<>("Humidity (%)");
        humidityCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getHumidity())));
        
        TableColumn<SensorReading, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTimestamp().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        TableColumn<SensorReading, String> riskLevelCol = new TableColumn<>("Risk Level");
        riskLevelCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRiskLevel() != null ? 
                                cellData.getValue().getRiskLevel().name() : "Unknown"));
        
        TableColumn<SensorReading, String> riskProbCol = new TableColumn<>("Risk Probability");
        riskProbCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f", cellData.getValue().getRiskProbability())));
        
        // Add columns to table
        readingsTable.getColumns().addAll(
                sensorIdCol, locationCol, tempCol, humidityCol, 
                timestampCol, riskLevelCol, riskProbCol);
        
        // Set items
        readingsTable.setItems(readings);
        
        // Control panel
        HBox controls = new HBox(10);
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshReadings());
        
        controls.getChildren().add(refreshButton);
        
        // Add to panel
        panel.getChildren().addAll(controls, readingsTable);
        
        return panel;
    }
    
    /**
     * Start periodic data updates.
     */
    private void startDataUpdates() {
        // Initial data load
        refreshReadings();
        
        // Start scheduler for periodic updates
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::updateUI);
        }, 0, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Refresh the readings table with latest data.
     */
    private void refreshReadings() {
        try {
            // Get latest readings from database
            List<SensorReading> latestReadings = database.getAllSensorReadings();
            
            Platform.runLater(() -> {
                readings.clear();
                readings.addAll(latestReadings);
                
                // Update charts
                updateCharts(latestReadings);
                
                // Update status
                if (!latestReadings.isEmpty()) {
                    SensorReading latest = latestReadings.get(0);
                    for (SensorReading reading : latestReadings) {
                        if (reading.getTimestamp().isAfter(latest.getTimestamp())) {
                            latest = reading;
                        }
                    }
                    
                    updateStatus(latest);
                }
            });
        } catch (Exception e) {
            logger.error("Error refreshing readings: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Update the UI with latest information.
     */
    private void updateUI() {
        // Update status text
        statusLabel.setText("Status: Connected");
        
        // Update model accuracy if available
        if (predictor != null && predictor.isTrained()) {
            ModelEvaluationResult metrics = predictor.getModelMetrics();
            if (metrics != null) {
                accuracyLabel.setText(String.format("Model Accuracy: %.2f%%", metrics.getAccuracy() * 100));
            }
        }
    }
    
    /**
     * Update charts with latest readings.
     * 
     * @param latestReadings the latest readings
     */
    private void updateCharts(List<SensorReading> latestReadings) {
        // Clear series
        tempSeries.getData().clear();
        humiditySeries.getData().clear();
        
        // Add data points
        int timePoint = 0;
        for (int i = latestReadings.size() - 1; i >= 0; i--) {
            SensorReading reading = latestReadings.get(i);
            
            tempSeries.getData().add(new XYChart.Data<>(timePoint, reading.getTemperature()));
            humiditySeries.getData().add(new XYChart.Data<>(timePoint, reading.getHumidity()));
            
            timePoint++;
            
            // Limit to last 20 points
            if (timePoint >= 20) break;
        }
    }
    
    /**
     * Update status information based on latest reading.
     * 
     * @param reading the latest reading
     */
    private void updateStatus(SensorReading reading) {
        FireRiskLevel riskLevel = reading.getRiskLevel();
        if (riskLevel == null) {
            riskLevel = FireRiskLevel.fromTemperature(reading.getTemperature());
        }
        
        riskLabel.setText("Current Risk: " + riskLevel.name());
        riskLabel.setTextFill(riskLevel.getDisplayColor());
    }
    
    /**
     * Stop the UI and release resources.
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
