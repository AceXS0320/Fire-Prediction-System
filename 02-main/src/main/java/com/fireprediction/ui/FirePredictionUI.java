package com.fireprediction.ui;

import com.fireprediction.database.Database;
import com.fireprediction.ml.FireRiskPredictor;
import com.fireprediction.ml.ModelEvaluationResult;
import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX user interface for the Fire Prediction System.
 * Modern UI implementation with enhanced visualization and user experience.
 */
public class FirePredictionUI {

    private static final Logger logger = LoggerFactory.getLogger(FirePredictionUI.class);
    
    private final Database database;
    private final FireRiskPredictor predictor;
    
    // UI components
    private TableView<SensorReading> readingsTable;
    private ObservableList<SensorReading> readings = FXCollections.observableArrayList();
    private LineChart<Number, Number> temperatureChart;
    private LineChart<Number, Number> humidityChart;
    private PieChart riskDistributionChart;
    private Label statusLabel;
    private Label riskLabel;
    private Label accuracyLabel;
    private Label dateLabel;
    private Label timeLabel;
    
    // Navigation components
    private Button dashboardButton;
    private Button readingsButton;
    private Button analyticsButton;
    private Button settingsButton;
    
    // Content panels
    private VBox dashboardPanel;
    private VBox readingsPanel;
    private VBox analyticsPanel;
    private VBox settingsPanel;
    private StackPane mainContent;
    
    // Chart data
    private XYChart.Series<Number, Number> tempSeries;
    private XYChart.Series<Number, Number> humiditySeries;
    
    // Update scheduler
    private ScheduledExecutorService scheduler;
    
    // UI colors - Modern Fire Prediction Theme
    private static final String SIDEBAR_COLOR = "#1E2A38";
    private static final String MAIN_BG_COLOR = "#0F1923";
    private static final String CARD_BG_COLOR = "#1E2A38";
    private static final String ACCENT_COLOR = "#FF5722";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String WARNING_COLOR = "#FFC107";
    private static final String DANGER_COLOR = "#F44336";
    private static final String TEXT_COLOR = "#FFFFFF";
    private static final String SECONDARY_TEXT_COLOR = "#B0BEC5";
    // Removed unused color constants
    
    // UI constants
    private static final String APP_TITLE = "Fire Prediction System";
    
    // User information
    private static final String USERNAME = "3amohom";
    private static final String USER_LEVEL = "INTERMEDIATE";
    
    /**
     * Create a new FirePredictionUI.
     * 
     * @param database the database
     * @param predictor the predictor
     */
    public FirePredictionUI(Database database, FireRiskPredictor predictor) {
        this.database = database;
        this.predictor = predictor;
        
        // Initialize charts to prevent NullPointerExceptions
        initializeCharts();
    }
    
    /**
     * Initialize charts to prevent NullPointerExceptions
     */
    private void initializeCharts() {
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
        
        // Create risk distribution chart
        riskDistributionChart = new PieChart();
        riskDistributionChart.setTitle("Risk Level Distribution");
        riskDistributionChart.setLabelsVisible(true);
    }
    
    /**
     * Start the UI.
     * 
     * @param primaryStage the primary stage
     */
    public void start(Stage primaryStage) {
        // Set stage properties
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Create UI components
        createUI();
        
        // Create scene with root container
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root"); // Add root style class
        
        // Create sidebar
        VBox sidebar = createSidebar();
        sidebar.setStyle("-fx-background-color: " + SIDEBAR_COLOR + ";");
        sidebar.setPadding(new Insets(20));
        sidebar.getChildren().addAll(dashboardButton, readingsButton, analyticsButton, settingsButton);
        
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        
        // Apply styles directly
        root.setStyle("-fx-background-color: " + MAIN_BG_COLOR + ";");
        mainContent.setStyle("-fx-background-color: " + MAIN_BG_COLOR + ";");
        
        // Create scene
        Scene scene = new Scene(root, 1200, 800);
        
        // Load CSS - Try the fixed CSS file first
        URL cssUrl = getClass().getResource("/styles/modern-fire-prediction-fixed.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            logger.info("Fixed CSS loaded successfully from: " + cssUrl.toExternalForm());
        } else {
            // Try alternate method to load the fixed CSS
            File cssFile = new File("src/main/resources/styles/modern-fire-prediction-fixed.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toString());
                logger.info("Fixed CSS loaded successfully from file: " + cssFile.getAbsolutePath());
            } else {
                // Fall back to the original CSS file
                URL originalCssUrl = getClass().getResource("/styles/modern-fire-prediction.css");
                if (originalCssUrl != null) {
                    scene.getStylesheets().add(originalCssUrl.toExternalForm());
                    logger.info("Original CSS loaded successfully from: " + originalCssUrl.toExternalForm());
                } else {
                    // Try alternate method for original CSS
                    File originalCssFile = new File("src/main/resources/styles/modern-fire-prediction.css");
                    if (originalCssFile.exists()) {
                        scene.getStylesheets().add(originalCssFile.toURI().toString());
                        logger.info("Original CSS loaded successfully from file: " + originalCssFile.getAbsolutePath());
                    } else {
                        logger.error("No CSS file found! Applying direct styles as fallback.");
                        // Apply more inline styles as fallback
                        applyDirectStyles(root);
                    }
                }
            }
        }
        
        // Set the scene and show the stage
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Start data updates
        startDataUpdates();
        
        // Update UI with initial data
        updateUI();
        
        logger.info("Fire Prediction UI started successfully");
    }
    
    /**
     * Create the UI components.
     */
    private void createUI() {
        // Create content panels
        mainContent = new StackPane();
        mainContent.setPadding(new Insets(20));
        
        // Create navigation buttons
        dashboardButton = createNavigationButton("Dashboard", true);
        readingsButton = createNavigationButton("Sensor Readings", false);
        analyticsButton = createNavigationButton("Analytics", false);
        settingsButton = createNavigationButton("Settings", false);
        
        // Create panels
        dashboardPanel = createDashboardPanel();
        readingsPanel = createReadingsPanel();
        analyticsPanel = createAnalyticsPanel();
        settingsPanel = createSettingsPanel();
        
        // Add panels to main content
        mainContent.getChildren().addAll(dashboardPanel, readingsPanel, analyticsPanel, settingsPanel);
        
        // Show dashboard initially
        showPanel(dashboardPanel);
        
        // Set up button actions
        dashboardButton.setOnAction(e -> {
            showPanel(dashboardPanel);
            dashboardButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
            readingsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            analyticsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            settingsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
        });
        
        readingsButton.setOnAction(e -> {
            showPanel(readingsPanel);
            dashboardButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            readingsButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
            analyticsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            settingsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
        });
        
        analyticsButton.setOnAction(e -> {
            showPanel(analyticsPanel);
            dashboardButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            readingsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            analyticsButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
            settingsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
        });
        
        settingsButton.setOnAction(e -> {
            showPanel(settingsPanel);
            dashboardButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            readingsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            analyticsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
            settingsButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
        });
        
        // Initialize charts
        initializeCharts();
    }
    
    /**
     * Create the analytics panel.
     * 
     * @return the analytics panel
     */
    private VBox createAnalyticsPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: " + MAIN_BG_COLOR + ";");
        
        // Header
        HBox statusBar = new HBox(20);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        // Date and time
        VBox dateTimeBox = new VBox(5);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);
        
        dateLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        dateLabel.setTextFill(Color.WHITE);
        
        timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        timeLabel.setFont(Font.font("System", 14));
        timeLabel.setTextFill(Color.web(SECONDARY_TEXT_COLOR));
        
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);
        
        // System status
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER);
        
        Label statusTitle = new Label("System Status");
        statusTitle.setFont(Font.font("System", 12));
        statusTitle.setTextFill(Color.web(SECONDARY_TEXT_COLOR));
        
        statusLabel = new Label("Online");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web(SUCCESS_COLOR));
        
        statusBox.getChildren().addAll(statusTitle, statusLabel);
        
        // Current risk level
        VBox riskBox = new VBox(5);
        riskBox.setAlignment(Pos.CENTER);
        
        Label riskTitle = new Label("Current Risk Level");
        riskTitle.setFont(Font.font("System", 12));
        riskTitle.setTextFill(Color.web(SECONDARY_TEXT_COLOR));
        
        riskLabel = new Label("Moderate");
        riskLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        riskLabel.setTextFill(Color.web(WARNING_COLOR));
        
        Label riskDescription = new Label("Increased vigilance required");
        riskDescription.setFont(Font.font("System", 12));
        riskDescription.setTextFill(Color.web(SECONDARY_TEXT_COLOR));
        
        riskBox.getChildren().addAll(riskTitle, riskLabel, riskDescription);
        
        // Model accuracy
        VBox accuracyBox = new VBox(5);
        accuracyBox.setAlignment(Pos.CENTER);
        
        accuracyLabel = new Label("Model Accuracy: 97.8%");
        accuracyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label confidenceLabel = new Label("High Confidence");
        confidenceLabel.setFont(Font.font("System", 12));
        
        accuracyBox.getChildren().addAll(accuracyLabel, confidenceLabel);
        
        // Add spacer to push refresh button to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Refresh button
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshReadings());
        
        statusBar.getChildren().addAll(dateTimeBox, statusBox, riskBox, accuracyBox, spacer, refreshButton);
        panel.getChildren().add(statusBar);
        
        // Add charts section
        Label chartsTitle = new Label("Analytics and Predictions");
        chartsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        chartsTitle.setTextFill(Color.WHITE);
        chartsTitle.setPadding(new Insets(10, 0, 10, 0));
        panel.getChildren().add(chartsTitle);
        
        // Model evaluation metrics
        HBox metricsBox = new HBox(20);
        metricsBox.setAlignment(Pos.CENTER);
        
        // Precision card
        VBox precisionCard = createMetricCard("Precision", "96.4%", "2.1% higher than last month", SUCCESS_COLOR);
        precisionCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        precisionCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        precisionCard.setPadding(new Insets(20));
        precisionCard.setPrefWidth(250);
        
        // Recall card
        VBox recallCard = createMetricCard("Recall", "94.2%", "1.5% higher than last month", SUCCESS_COLOR);
        recallCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        recallCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        recallCard.setPadding(new Insets(20));
        recallCard.setPrefWidth(250);
        
        // F1 Score card
        VBox f1Card = createMetricCard("F1 Score", "95.3%", "1.8% higher than last month", SUCCESS_COLOR);
        f1Card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        f1Card.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        f1Card.setPadding(new Insets(20));
        f1Card.setPrefWidth(250);
        
        metricsBox.getChildren().addAll(precisionCard, recallCard, f1Card);
        panel.getChildren().add(metricsBox);
        
        return panel;
    }
    
    /**
     * Create the dashboard panel with charts.
     * 
     * @return the dashboard panel
     */
    private VBox createDashboardPanel() {
        VBox dashboardPanel = new VBox(20);
        dashboardPanel.setPadding(new Insets(20));
        dashboardPanel.setStyle("-fx-background-color: " + MAIN_BG_COLOR + ";");
        
        // Create header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        VBox titleBox = new VBox(5);
        Label title = new Label("Fire Prediction System");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Real-time monitoring and risk assessment");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web(SECONDARY_TEXT_COLOR));
        
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // System status indicator
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label statusLabel = new Label("System Status");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web(SECONDARY_TEXT_COLOR));
        
        Label statusValue = new Label("Online");
        statusValue.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusValue.setTextFill(Color.web(SUCCESS_COLOR));
        
        statusBox.getChildren().addAll(statusLabel, statusValue);
        
        header.getChildren().addAll(titleBox, spacer, statusBox);
        dashboardPanel.getChildren().add(header);
        
        // Current readings section
        Label readingsTitle = new Label("Current Sensor Readings");
        readingsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        readingsTitle.setTextFill(Color.WHITE);
        readingsTitle.setPadding(new Insets(0, 0, 10, 0));
        dashboardPanel.getChildren().add(readingsTitle);
        
        // Metric cards
        HBox currentReadingsBox = new HBox(20);
        currentReadingsBox.setAlignment(Pos.CENTER);
        
        // Temperature card
        VBox tempCard = createMetricCard("Current Temperature", "32.5°C", "1.2°C higher than yesterday", WARNING_COLOR);
        tempCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        tempCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        tempCard.setPadding(new Insets(20));
        tempCard.setPrefWidth(250);
        
        // Humidity card
        VBox humidityCard = createMetricCard("Current Humidity", "45.8%", "5.3% lower than yesterday", DANGER_COLOR);
        humidityCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        humidityCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        humidityCard.setPadding(new Insets(20));
        humidityCard.setPrefWidth(250);
        
        // Wind speed card
        VBox windCard = createMetricCard("Wind Speed", "15 km/h", "Moderate", SUCCESS_COLOR);
        windCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        windCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        windCard.setPadding(new Insets(20));
        windCard.setPrefWidth(250);
        
        // Risk level card
        VBox riskCard = createMetricCard("Current Risk Level", "Moderate", "Increased from Low", WARNING_COLOR);
        riskCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        riskCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        riskCard.setPadding(new Insets(20));
        riskCard.setPrefWidth(250);
        
        currentReadingsBox.getChildren().addAll(tempCard, humidityCard, windCard, riskCard);
        dashboardPanel.getChildren().add(currentReadingsBox);
        
        // Charts section title
        Label chartsTitle = new Label("Sensor Data Trends");
        chartsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        chartsTitle.setTextFill(Color.WHITE);
        chartsTitle.setPadding(new Insets(20, 0, 10, 0));
        dashboardPanel.getChildren().add(chartsTitle);
        
        // Charts section
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        
        // Temperature chart
        VBox tempChartBox = new VBox(10);
        tempChartBox.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        tempChartBox.setPadding(new Insets(15));
        tempChartBox.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        tempChartBox.setPrefWidth(550);
        
        Label tempChartTitle = new Label("Temperature Trends");
        tempChartTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tempChartTitle.setTextFill(Color.WHITE);
        
        // Style the temperature chart
        temperatureChart.setAnimated(false);
        temperatureChart.setCreateSymbols(true);
        temperatureChart.setLegendVisible(false);
        temperatureChart.setPrefHeight(250);
        
        ((NumberAxis) temperatureChart.getXAxis()).setTickLabelFill(Color.web(SECONDARY_TEXT_COLOR));
        ((NumberAxis) temperatureChart.getYAxis()).setTickLabelFill(Color.web(SECONDARY_TEXT_COLOR));
        temperatureChart.getXAxis().setTickLabelGap(10);
        temperatureChart.getYAxis().setTickLabelGap(10);
        
        temperatureChart.setStyle("-fx-background-color: transparent;");
        
        tempChartBox.getChildren().addAll(tempChartTitle, temperatureChart);
        
        // Humidity chart
        VBox humidityChartBox = new VBox(10);
        humidityChartBox.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        humidityChartBox.setPadding(new Insets(15));
        humidityChartBox.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        humidityChartBox.setPrefWidth(550);
        
        Label humidityChartTitle = new Label("Humidity Trends");
        humidityChartTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        humidityChartTitle.setTextFill(Color.WHITE);
        
        // Style the humidity chart
        humidityChart.setAnimated(false);
        humidityChart.setCreateSymbols(true);
        humidityChart.setLegendVisible(false);
        humidityChart.setPrefHeight(250);
        
        ((NumberAxis) humidityChart.getXAxis()).setTickLabelFill(Color.web(SECONDARY_TEXT_COLOR));
        ((NumberAxis) humidityChart.getYAxis()).setTickLabelFill(Color.web(SECONDARY_TEXT_COLOR));
        humidityChart.getXAxis().setTickLabelGap(10);
        humidityChart.getYAxis().setTickLabelGap(10);
        
        humidityChart.setStyle("-fx-background-color: transparent;");
        
        humidityChartBox.getChildren().addAll(humidityChartTitle, humidityChart);
        
        chartsBox.getChildren().addAll(tempChartBox, humidityChartBox);
        dashboardPanel.getChildren().add(chartsBox);
        
        // Risk distribution section title
        Label riskDistSectionTitle = new Label("Fire Risk Distribution");
        riskDistSectionTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        riskDistSectionTitle.setTextFill(Color.WHITE);
        riskDistSectionTitle.setPadding(new Insets(20, 0, 10, 0));
        dashboardPanel.getChildren().add(riskDistSectionTitle);
        
        // Risk distribution chart container
        VBox riskDistributionBox = new VBox(10);
        riskDistributionBox.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        riskDistributionBox.setPadding(new Insets(15));
        riskDistributionBox.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        riskDistributionBox.setAlignment(Pos.CENTER);
        
        Label riskDistTitle = new Label("Risk Level Distribution by Zone");
        riskDistTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        riskDistTitle.setTextFill(Color.WHITE);
        
        // Style the risk distribution chart
        riskDistributionChart.setLabelsVisible(true);
        riskDistributionChart.setLabelLineLength(20);
        riskDistributionChart.setStyle("-fx-background-color: transparent;");
        riskDistributionChart.setPrefHeight(300);
        
        // Sample data for risk distribution
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Low Risk", 45),
            new PieChart.Data("Moderate Risk", 30),
            new PieChart.Data("High Risk", 15),
            new PieChart.Data("Critical Risk", 10)
        );
        
        riskDistributionChart.setData(pieChartData);
        
        // Style the pie chart slices
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: " + SUCCESS_COLOR + ";");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: " + WARNING_COLOR + ";");
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: " + ACCENT_COLOR + ";");
        pieChartData.get(3).getNode().setStyle("-fx-pie-color: " + DANGER_COLOR + ";");
        
        riskDistributionBox.getChildren().addAll(riskDistTitle, riskDistributionChart);
        dashboardPanel.getChildren().add(riskDistributionBox);
        
        // Risk levels section
        Label riskLevelsHeader = new Label("Fire Risk Levels");
        riskLevelsHeader.setFont(Font.font("System", FontWeight.BOLD, 20));
        riskLevelsHeader.setTextFill(Color.WHITE);
        riskLevelsHeader.setPadding(new Insets(20, 0, 10, 0));
        dashboardPanel.getChildren().add(riskLevelsHeader);
        
        VBox riskLevelsCard = new VBox(10);
        riskLevelsCard.setPadding(new Insets(15));
        riskLevelsCard.setStyle("-fx-background-color: " + CARD_BG_COLOR + "; -fx-background-radius: 10px;");
        riskLevelsCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        
        Label riskLevelsTitle = new Label("Risk Level Indicators");
        riskLevelsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        riskLevelsTitle.setTextFill(Color.WHITE);
        
        // Create risk level indicators
        VBox riskLevelsBox = new VBox(8);
        riskLevelsBox.getChildren().addAll(
            createRiskLevelIndicator("Low Risk", SUCCESS_COLOR),
            createRiskLevelIndicator("Moderate Risk", WARNING_COLOR),
            createRiskLevelIndicator("High Risk", ACCENT_COLOR),
            createRiskLevelIndicator("Critical Risk", DANGER_COLOR)
        );
        
        riskLevelsCard.getChildren().addAll(riskLevelsTitle, riskLevelsBox);
        dashboardPanel.getChildren().add(riskLevelsCard);
        
        return dashboardPanel;
    }
    
    /**
     * Create a metric card with title, value, and trend.
     * 
     * @param title the metric title
     * @param value the metric value
     * @param trend the trend description or unit
     * @param trendColor the color for the trend (or null for unit display)
     * @return the metric card
     */
    private VBox createMetricCard(String title, String value, String trend, String trendColor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("metric-card");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        
        Label trendLabel = new Label(trend);
        if (trendColor != null) {
            trendLabel.setTextFill(Color.web(trendColor));
            trendLabel.getStyleClass().add("metric-trend");
            if (trendColor.equals("#4CAF50")) {
                trendLabel.getStyleClass().add("up");
            } else if (trendColor.equals("#F44336")) {
                trendLabel.getStyleClass().add("down");
            }
        } else {
            trendLabel.getStyleClass().add("metric-unit");
        }
        
        card.getChildren().addAll(titleLabel, valueLabel, trendLabel);
        return card;
    }
    
    /**
     * Create the settings panel.
     * 
     * @return the settings panel
     */
    private VBox createSettingsPanel() {
        VBox settingsPanel = new VBox(20);
        settingsPanel.setPadding(new Insets(20));
        settingsPanel.getStyleClass().add("settings-section");
        
        Label titleLabel = new Label("Settings");
        titleLabel.getStyleClass().add("settings-title");
        
        // Notification settings section
        VBox notificationSection = new VBox(10);
        notificationSection.getStyleClass().add("settings-form");
        
        Label notificationLabel = new Label("Notification Settings");
        notificationLabel.getStyleClass().add("settings-subtitle");
        
        CheckBox emailNotifications = new CheckBox("Email Notifications");
        emailNotifications.setSelected(true);
        
        CheckBox smsNotifications = new CheckBox("SMS Notifications");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefWidth(300);
        
        // Alert threshold settings
        VBox thresholdSection = new VBox(10);
        thresholdSection.getStyleClass().add("settings-form");
        
        Label thresholdLabel = new Label("Alert Thresholds");
        thresholdLabel.getStyleClass().add("settings-subtitle");
        
        HBox tempThreshold = new HBox(10);
        tempThreshold.setAlignment(Pos.CENTER_LEFT);
        Label tempLabel = new Label("Temperature Threshold (°C):");
        Slider tempSlider = new Slider(30, 60, 45);
        tempSlider.setShowTickLabels(true);
        tempSlider.setShowTickMarks(true);
        tempSlider.setMajorTickUnit(10);
        tempSlider.setMinorTickCount(5);
        tempSlider.setPrefWidth(200);
        Label tempValueLabel = new Label("45°C");
        tempThreshold.getChildren().addAll(tempLabel, tempSlider, tempValueLabel);
        
        // Update the temperature value label when the slider changes
        tempSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            tempValueLabel.setText(String.format("%.0f°C", newVal.doubleValue()));
        });
        
        // Save button
        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            // Save settings logic would go here
            // Display a simple alert instead of using showNotification
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Saved");
            alert.setHeaderText(null);
            alert.setContentText("Settings saved successfully");
            alert.showAndWait();
        });
        
        // Add all components to their respective sections
        notificationSection.getChildren().addAll(notificationLabel, emailNotifications, smsNotifications, emailField);
        thresholdSection.getChildren().addAll(thresholdLabel, tempThreshold);
        
        // Add all sections to the main settings panel
        settingsPanel.getChildren().addAll(titleLabel, notificationSection, thresholdSection, saveButton);
        
        return settingsPanel;
    }
    
    /**
     * Create the readings panel with table.
     * 
     * @return the readings panel
     */
    private VBox createReadingsPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(10));
        
        // Panel title
        Label title = new Label("Sensor Readings & Fire Risk Analysis");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        panel.getChildren().add(title);
        
        // Sensor status cards
        HBox sensorStatusBox = new HBox(20);
        sensorStatusBox.setPrefHeight(120);
        
        VBox activeSensorsCard = createMetricCard("Active Sensors", "8", "All sensors operational", SUCCESS_COLOR);
        VBox lastUpdateCard = createMetricCard("Last Update", "2 min ago", "Data is current", SUCCESS_COLOR);
        VBox avgTempCard = createMetricCard("Avg Temperature", "29.8 °C", "Rising trend detected", WARNING_COLOR);
        VBox avgHumidityCard = createMetricCard("Avg Humidity", "52.3%", "Stable conditions", SUCCESS_COLOR);
        
        sensorStatusBox.getChildren().addAll(activeSensorsCard, lastUpdateCard, avgTempCard, avgHumidityCard);
        panel.getChildren().add(sensorStatusBox);
        
        // Filter controls
        HBox controls = new HBox(15);
        controls.setPadding(new Insets(10, 0, 10, 0));
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by:");
        filterLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        ComboBox<String> locationFilter = new ComboBox<>();
        locationFilter.getItems().addAll("All Locations", "North Zone", "South Zone", "East Zone", "West Zone");
        locationFilter.setValue("All Locations");
        locationFilter.setPrefWidth(150);
        
        ComboBox<String> riskFilter = new ComboBox<>();
        riskFilter.getItems().addAll("All Risk Levels", "LOW", "MODERATE", "HIGH", "EXTREME");
        riskFilter.setValue("All Risk Levels");
        riskFilter.setPrefWidth(150);
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Filter by date");
        
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshReadings());
        
        Button exportButton = new Button("Export Data");
        exportButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        controls.getChildren().addAll(filterLabel, locationFilter, riskFilter, datePicker, refreshButton, exportButton);
        panel.getChildren().add(controls);
        
        // Create table with modern styling
        readingsTable = new TableView<>();
        readingsTable.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        readingsTable.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));
        
        // Columns
        TableColumn<SensorReading, String> sensorIdCol = new TableColumn<>("Sensor ID");
        sensorIdCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSensorId()));
        sensorIdCol.setPrefWidth(100);
        
        TableColumn<SensorReading, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLocation()));
        locationCol.setPrefWidth(150);
        
        TableColumn<SensorReading, String> tempCol = new TableColumn<>("Temperature (°C)");
        tempCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getTemperature())));
        tempCol.setPrefWidth(130);
        
        TableColumn<SensorReading, String> humidityCol = new TableColumn<>("Humidity (%)");
        humidityCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", cellData.getValue().getHumidity())));
        humidityCol.setPrefWidth(120);
        
        TableColumn<SensorReading, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTimestamp().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        timestampCol.setPrefWidth(180);
        
        TableColumn<SensorReading, String> riskLevelCol = new TableColumn<>("Risk Level");
        riskLevelCol.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRiskLevel() != null ? 
                                cellData.getValue().getRiskLevel().name() : "Unknown"));
        riskLevelCol.setPrefWidth(120);
        
        // Custom cell factory for risk level column to show colors
        riskLevelCol.setCellFactory(column -> {
            return new TableCell<SensorReading, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        
                        // Set background color based on risk level
                        if (item.equals("LOW")) {
                            setStyle("-fx-background-color: " + SUCCESS_COLOR + "33; -fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                        } else if (item.equals("MODERATE")) {
                            setStyle("-fx-background-color: " + WARNING_COLOR + "33; -fx-text-fill: " + WARNING_COLOR + "; -fx-font-weight: bold;");
                        } else if (item.equals("HIGH")) {
                            setStyle("-fx-background-color: #FF980033; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        } else if (item.equals("EXTREME")) {
                            setStyle("-fx-background-color: " + DANGER_COLOR + "33; -fx-text-fill: " + DANGER_COLOR + "; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
        
        TableColumn<SensorReading, String> riskProbCol = new TableColumn<>("Risk Probability");
        riskProbCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                        String.format("%.2f", cellData.getValue().getRiskProbability())));
        riskProbCol.setPrefWidth(130);
        
        // Add columns to table one by one to avoid type safety warning
        readingsTable.getColumns().add(sensorIdCol);
        readingsTable.getColumns().add(locationCol);
        readingsTable.getColumns().add(tempCol);
        readingsTable.getColumns().add(humidityCol);
        readingsTable.getColumns().add(timestampCol);
        readingsTable.getColumns().add(riskLevelCol);
        readingsTable.getColumns().add(riskProbCol);
        
        // Set items
        readingsTable.setItems(readings);
        
        // Add to panel
        panel.getChildren().add(readingsTable);
        
        // Summary section
        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label summaryLabel = new Label("Summary: ");
        summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label summaryText = new Label("8 sensors active, monitoring 4 zones. Current risk assessment shows predominantly LOW risk levels with some MODERATE risk in the East Zone. Last critical alert: None in the past 24 hours.");
        summaryText.setWrapText(true);
        
        summaryBox.getChildren().addAll(summaryLabel, summaryText);
        panel.getChildren().add(summaryBox);
        
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
            
            // Show error in UI
            Platform.runLater(() -> {
                statusLabel.setText("Connection Error");
                statusLabel.setTextFill(Color.web(DANGER_COLOR));
            });
        }
    }
    
    /**
     * Update the UI with latest information.
     */
    private void updateUI() {
        // Update status text
        statusLabel.setText("System Active");
        statusLabel.setTextFill(Color.web(SUCCESS_COLOR));
        
        // Update model accuracy if available
        if (predictor != null && predictor.isTrained()) {
            ModelEvaluationResult metrics = predictor.getModelMetrics();
            if (metrics != null) {
                accuracyLabel.setText(String.format("Model Accuracy: %.2f%%", metrics.getAccuracy() * 100));
            }
        }
        
        // Update time
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
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
        
        // Update risk distribution chart
        updateRiskDistribution(latestReadings);
    }
    
    /**
     * Update risk distribution chart with latest data.
     * 
     * @param latestReadings the latest readings
     */
    private void updateRiskDistribution(List<SensorReading> latestReadings) {
        // In a real app, this would calculate actual distribution from readings
    }
    
    /**
     * Create the sidebar navigation panel.
     * 
     * @return the sidebar VBox
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");
        
        // Create user profile section
        VBox userProfile = new VBox(5);
        userProfile.getStyleClass().add("user-profile");
        userProfile.setPadding(new Insets(0, 0, 15, 0));
        
        Label usernameLabel = new Label(USERNAME);
        usernameLabel.getStyleClass().add("username-label");
        usernameLabel.setTextFill(Color.WHITE);
        
        Label userLevelLabel = new Label(USER_LEVEL);
        userLevelLabel.getStyleClass().add("user-level");
        userLevelLabel.setTextFill(Color.web(ACCENT_COLOR));
        
        userProfile.getChildren().addAll(usernameLabel, userLevelLabel);
        
        // Add spacing between user profile and navigation buttons
        sidebar.getChildren().add(userProfile);
        sidebar.getChildren().add(new Separator());
        
        return sidebar;
    }

    /**
     * Show the specified panel and hide all others.
     * 
     * @param panelToShow the panel to show
     */
    private void showPanel(Pane panelToShow) {
        // Hide all panels
        dashboardPanel.setVisible(false);
        readingsPanel.setVisible(false);
        analyticsPanel.setVisible(false);
        settingsPanel.setVisible(false);
        
        // Show the selected panel
        panelToShow.setVisible(true);
        
        // Update button styles
        Button[] buttons = {
            dashboardButton, readingsButton, analyticsButton, settingsButton
        };
        
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
        
        // Set the active button
        if (panelToShow == dashboardPanel) dashboardButton.getStyleClass().add("active");
        else if (panelToShow == readingsPanel) readingsButton.getStyleClass().add("active");
        else if (panelToShow == analyticsPanel) analyticsButton.getStyleClass().add("active");
        else if (panelToShow == settingsPanel) settingsButton.getStyleClass().add("active");
    }
    /**
     * Create a navigation button for the sidebar.
     * 
     * @param text the button text
     * @param active whether the button is initially active
     * @return the button
     */
    private Button createNavigationButton(String text, boolean active) {
        Button button = new Button(text);
        button.setPrefWidth(180);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(0, 0, 0, 15));
        button.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        button.getStyleClass().add("nav-button");
        
        if (active) {
            button.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
            button.getStyleClass().add("active");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + ";");
        }
        
        return button;
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
        
        // Apply direct styles based on risk level
        VBox statusPanel = (VBox) riskLabel.getParent();
        
        switch (riskLevel) {
            case LOW:
                statusPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(76, 175, 80, 0.9), rgba(56, 142, 60, 0.9));"
                                    + "-fx-background-radius: 10px; -fx-padding: 15px;");
                break;
            case MODERATE:
                statusPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(255, 193, 7, 0.9), rgba(251, 140, 0, 0.9));"
                                    + "-fx-background-radius: 10px; -fx-padding: 15px;");
                break;
            case HIGH:
                statusPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(255, 152, 0, 0.9), rgba(230, 81, 0, 0.9));"
                                    + "-fx-background-radius: 10px; -fx-padding: 15px;");
                break;
            case EXTREME:
                statusPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(244, 67, 54, 0.9), rgba(211, 47, 47, 0.9));"
                                    + "-fx-background-radius: 10px; -fx-padding: 15px;"
                                    + "-fx-effect: dropshadow(gaussian, rgba(244, 67, 54, 0.5), 15, 0, 0, 5);");
                break;
        }
    }
    
    /**
     * Apply direct styles to UI components as a fallback when CSS is not available.
     * 
     * @param root the root container
     */
    private void applyDirectStyles(BorderPane root) {
        // Apply styles to main containers
        root.setStyle("-fx-background-color: " + MAIN_BG_COLOR + ";");
        mainContent.setStyle("-fx-background-color: " + MAIN_BG_COLOR + "; -fx-padding: 20px;");
        
        // Style buttons
        List<Button> allButtons = new ArrayList<>();
        allButtons.add(dashboardButton);
        allButtons.add(readingsButton);
        allButtons.add(analyticsButton);
        allButtons.add(settingsButton);
        
        for (Button button : allButtons) {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_COLOR + "; "
                          + "-fx-padding: 10px 15px; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");
        }
        
        // Style panels
        List<Pane> panels = new ArrayList<>();
        panels.add(dashboardPanel);
        panels.add(readingsPanel);
        panels.add(analyticsPanel);
        panels.add(settingsPanel);
        
        for (Pane panel : panels) {
            panel.setStyle("-fx-background-color: " + MAIN_BG_COLOR + "; -fx-padding: 20px;");
        }
    }
    
    private HBox createRiskLevelIndicator(String text, String color) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        
        Circle indicator = new Circle(6);
        indicator.setFill(Color.web(color));
        
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        
        container.getChildren().addAll(indicator, label);
        return container;
    }
    
    // Removed unused createSettingField method
    
    // Removed enrolled courses panel creation method
    
    // Removed top rated courses panel creation method
    
    // Removed course card creation method
    
    // Removed course progress bar creation method
    
    // Removed enrolled course helper class
}
