# Fire Prediction System

A sophisticated fire prediction system that integrates IoT sensors (ESP32), machine learning (SMILE ML), and a modern JavaFX UI to detect and predict potential fire hazards.

## Features

- **Real-time Sensor Monitoring**: Collects temperature and humidity data from ESP32 with DHT22 sensors
- **Smart Fire Risk Prediction**: Uses SMILE ML to predict fire risks based on environmental factors
- **Supabase Database Integration**: Stores historical sensor data for trend analysis
- **Modern JavaFX UI**: Interactive dashboards with real-time charts and risk visualizations
- **Simulation Mode**: Works without physical sensors, generating realistic data for testing
- **Comprehensive OOP Design**: Leverages inheritance, interfaces, encapsulation, and design patterns

## OOP Principles Implemented

This project extensively demonstrates core OOP principles:

- **Interfaces**: Clearly defined contracts like `Database`, `Sensor`, and `FireRiskPredictor` enable loose coupling
- **Polymorphism**: Multiple implementations of interfaces (e.g., real vs. simulated sensors) can be used interchangeably
- **Encapsulation**: Private fields with controlled access through getters/setters
- **Design Patterns**:
  - Factory Pattern: `PredictorFactory` encapsulates object creation
  - Strategy Pattern: Swappable ML implementations via the `FireRiskPredictor` interface
  - Façade Pattern: `SensorManager` provides a simplified interface to the sensor subsystem
  - Builder Pattern: Used in the `SensorReading` class through manual implementation

## System Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Optional: ESP32 microcontroller with DHT22 sensor (falls back to simulation if not available)
- Internet connection for Supabase database access

## Installation & Setup

1. Clone the repository:
   ```
   git clone https://your-repository-url.git
   cd fire-prediction-system
   ```

2. Build the project:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn javafx:run
   ```

### Quick Start Guide

1. Set the required environment variables:
   - For Windows Command Prompt:
     ```
    set SUPABASE_URL=https://osrlvvrncgxutuphvyom.supabase.co
    set SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zcmx2dnJuY2d4dXR1cGh2eW9tIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI1MjIwMTIsImV4cCI6MjA1ODA5ODAxMn0.RlhInajoY7Ta2SEFc3TMr5Y8moU3VSPCcA3yoCSvazg
    set DEFAULT_PREDICTOR=SMILE
    set SIMULATION_MODE_ENABLED=true
     ```

2. Run directly with Maven:
   ```
   mvn clean javafx:run
   ```

3. Alternatively, build and run the JAR file:
   ```
   mvn clean package
   java --module-path "PATH_TO_JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/fire-prediction-system-1.0-SNAPSHOT.jar
   ```
   
   **Note**: When running the JAR directly, you need to provide the JavaFX modules path since JavaFX is not included in the standard JDK.

4. For development and testing, use simulation mode by setting `SIMULATION_MODE_ENABLED=true`

## Configuration

The application uses environment variables for configuration:

```properties
# Supabase Configuration
SUPABASE_URL=https://osrlvvrncgxutuphvyom.supabase.co/
SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Application Configuration
APP_NAME=Fire Prediction System (optional)
SENSOR_READ_INTERVAL_SECONDS=30 (optional)
DEFAULT_PREDICTOR=DUMMY or SMILE
SIMULATION_MODE_ENABLED=true or false
```

## Usage Guide for Beginners

When you start the application, you'll see a window with three tabs:

### Dashboard Tab
- **Temperature Chart**: Shows real-time temperature readings from all sensors
- **Humidity Chart**: Shows real-time humidity readings from all sensors  
- **Latest Readings Table**: Displays the most recent data from each sensor

### Sensor Readings Tab
- **Data Table**: Shows all sensor readings with details
- **Refresh Button**: Updates the table with the latest data
- **Clear Button**: Clears the table (doesn't delete data from the database)

### Risk Levels Tab
- **Risk Information**: Explains what each risk level means
- **System Information**: Shows what predictor is being used and current operating mode

## For Complete Beginners: How It Works

### What's Happening Behind the Scenes?

1. **Simulated Sensors**: When you run in simulation mode, the program creates fake sensors that generate realistic temperature and humidity data
   
2. **Data Storage**: Each reading is automatically saved to a database (Supabase) in the cloud

3. **Risk Prediction**: The program analyzes each reading to determine the fire risk level:
   - LOW: Normal conditions, very little risk
   - MODERATE: Slightly elevated risk
   - HIGH: Significant risk, should be monitored
   - EXTREME: Dangerous conditions that require immediate attention

4. **Real-time Updates**: The charts and tables automatically update as new data comes in

### Database Setup
The application needs tables in the Supabase database to work properly. If you're getting errors about missing tables, you'll need to create:

1. A `sensor_readings` table to store temperature and humidity data
2. A `ml_models` table to store trained machine learning models

## Our Journey: From Lombok to Clean Build

For those interested in the development process, here's a beginner-friendly explanation of challenges we faced and how we solved them:

### What is Lombok and Why We Removed It

**Lombok** is a Java library that can automatically generate code like getters, setters, and constructors for you. Instead of writing these methods yourself, you add special annotations to your class, and Lombok creates the code during compilation.

While Lombok makes development faster, it also creates some problems:
- It's an extra dependency that can break when updating Java versions
- The generated code is "hidden," making it harder for beginners to understand
- It can cause issues with some IDEs and build tools

### Problems We Faced (For Beginners)

#### 1. The Lombok Removal Challenge

**Problem**: All the code used Lombok annotations instead of regular Java code.

**Solution**: We manually replaced all Lombok annotations with standard Java code:
- `@Getter/@Setter` → Created regular getter and setter methods
- `@Builder` → Implemented our own Builder pattern classes
- `@Slf4j` → Replaced with standard SLF4J logger declarations

**What This Means for Beginners**: The code is now more straightforward to read and understand. You can see exactly what methods are available without needing to know about Lombok's "magic."

#### 2. SMILE Machine Learning Integration

**Problem**: The SMILE library (Statistical Machine Intelligence and Learning Engine) has complex APIs that were challenging to integrate.

**Solution**: We:
- Fixed the dependencies in the `pom.xml` file
- Implemented proper predictor classes that use SMILE for machine learning
- Added a simpler "DUMMY" predictor as a fallback

**What This Means for Beginners**: You can use simple rule-based prediction (DUMMY) or more advanced machine learning (SMILE) for predictions.

#### 3. JavaFX UI Challenges

**Problem**: Creating an interactive, real-time updating UI is complex.

**Solution**: We:
- Created a tabbed interface with different views
- Implemented real-time charts that update as new data arrives
- Added a comprehensive dashboard to visualize sensor data
- Used JavaFX's property binding for automatic UI updates

**What This Means for Beginners**: You get a beautiful, interactive UI that shows you exactly what's happening with your sensors in real-time.

#### 4. Deployment Challenges

**Problem**: JavaFX applications are more complicated to package and distribute than regular Java applications.

**Solution**: We:
- Used Maven plugins to handle the JavaFX dependencies
- Provided instructions for running both through Maven and as a standalone JAR
- Documented the extra steps needed for running the JAR (the module path requirements)

**What This Means for Beginners**: You can run the application easily with Maven, but need additional steps if running the JAR directly.

## UI Enhancements

The Fire Prediction System's user interface has been enhanced with the following improvements:

### CSS Styling Improvements

- **Color Consistency**: Implemented a consistent color scheme using CSS variables for base colors, text colors, accent colors, and status colors
- **Dashboard Heading**: Enhanced visibility of headings with proper text color and transparent background to ensure readability
- **Table Styling**: Fixed the table styling to remove the white part after the last column by setting appropriate background colors and border colors
- **Risk Level Indicators**: Improved risk level indicators with bold text and appropriate colors:
  - LOW: Green with white text
  - MODERATE: Yellow with dark text
  - HIGH: Orange with dark text
  - EXTREME: Red with white text
- **Responsive Layout**: Enhanced the responsiveness of the layout to center content in full window mode
- **Visual Hierarchy**: Improved spacing, padding, and margins for better visual organization

### Technical Implementation Details

- Used JavaFX CSS styling with both `-fx-` prefixed properties and standard CSS properties for better compatibility
- Applied targeted styling to specific UI components using class selectors
- Implemented responsive containers with max-width and auto margins for proper centering
- Added subtle visual effects like shadows and rounded corners for a modern look

## Final Thoughts for Beginners

The Fire Prediction System demonstrates how to build a complete, real-world application with:

- **Modern Java**: Using the latest Java features without dependencies like Lombok
- **Clean Architecture**: Separating concerns using interfaces and proper packaging
- **Real-time Data**: Processing sensor data as it arrives
- **Data Visualization**: Showing trends and current status using charts and tables
- **Modern UI Design**: Creating a responsive, visually appealing interface with CSS
- **Machine Learning**: Using the SMILE library to make predictions

Even if you're new to programming, you can learn a lot by exploring this codebase and seeing how all these components work together to create a functioning system.

The code is well-commented and uses a consistent structure, making it an excellent learning resource for Java programming, OOP principles, and application design.

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── fireprediction/
│   │   │           ├── database/      # Database interface and implementation
│   │   │           ├── ml/            # Machine learning components
│   │   │           ├── model/         # Domain model classes
│   │   │           ├── sensor/        # Sensor interfaces and implementations
│   │   │           ├── ui/            # JavaFX UI components
│   │   │           └── FirePredictionApplication.java  # Main entry point
│   │   └── resources/
│   │       └── logback.xml           # Logging configuration
│   └── test/                         # Test classes
├── pom.xml                           # Maven configuration
└── README.md                         # This file

This Project was created by:
M0kh
Ace
El Kordy
Gargeer
(If you are reading this, just know this was one hell of a team :)
