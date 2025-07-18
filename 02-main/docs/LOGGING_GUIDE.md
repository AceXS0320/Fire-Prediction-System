# Understanding Log Files in the Fire Prediction System

## What Are Log Files?

Log files are like a diary or journal for your application. They automatically record what your program is doing, when it happened, and whether anything went wrong. Think of logs as breadcrumbs that help you trace what happened in your application.

## Why Do We Need Logging?

Imagine you're building a model airplane, but something goes wrong and it doesn't fly properly. Without watching every step of the process, how would you know what caused the problem?

In software, logging serves this purpose:

- **Troubleshooting**: When something goes wrong, logs help you figure out what happened
- **Monitoring**: Logs show if your application is working correctly
- **Auditing**: Logs keep a record of important events and actions
- **Debugging**: Logs help developers understand what's happening inside the application

## How Logging Works in Our Fire Prediction System

Our system uses a popular Java logging framework called **Logback**. Here's how it works in simple terms:

### 1. Log Levels

Logs come in different levels of importance (from least to most important):

- **DEBUG**: Detailed information useful for developers debugging the application
- **INFO**: General information about application progress
- **WARN**: Potentially harmful situations that might cause problems
- **ERROR**: Error events that might still allow the application to continue running
- **FATAL**: Very severe errors that will likely lead to application failure

Think of it like this:
- **DEBUG** is like noting "I'm checking if the sensor is connected"
- **INFO** is like "The Fire Prediction System has started successfully"
- **WARN** is "The temperature seems unusually high, but within acceptable range"
- **ERROR** is "Failed to connect to the database"
- **FATAL** is "System cannot continue operation due to critical failure"

### 2. Where Logs Are Stored

In our application, logs go to two places:

- **Console**: Logs appear directly in the terminal or command prompt window when you run the application
- **Log Files**: Stored in the `logs/` folder of your application as text files

### 3. Log File Organization

Our system organizes log files in a smart way:

- **Current Logs**: Stored in `fire-prediction.log`
- **Daily Archives**: Each day gets its own file named `fire-prediction.YYYY-MM-DD.log` (for example: `fire-prediction.2025-05-04.log`)
- **Automatic Cleanup**: Logs older than 30 days are automatically deleted to save disk space

### 4. What's In Our Log Files

Each line in a log file contains:

- **Timestamp**: When the event happened (e.g., `2025-05-04 14:02:33.123`)
- **Thread**: Which part of the program was running (e.g., `[main]`)
- **Level**: How important is this message (e.g., `INFO`, `ERROR`)
- **Logger**: Which component generated the message (e.g., `com.fireprediction.FirePredictionApplication`)
- **Message**: The actual information (e.g., "Starting Fire Prediction Application")

For example:
```
2025-05-04 14:02:33.123 [main] INFO com.fireprediction.FirePredictionApplication - Starting Fire Prediction Application
```

This tells us:
- At 2:02 PM on May 4, 2025
- In the main thread
- An informational message
- From the main application class
- The application is starting

### 5. Examples From Our Application

Let's look at some common examples you might see in our Fire Prediction System logs:

#### Application Startup
```
INFO com.fireprediction.FirePredictionApplication - Starting Fire Prediction Application
```
This tells us the application has started successfully.

#### Sensor Reading
```
DEBUG com.fireprediction.sensor.SensorManager - Received reading: temperature=24.5°C, humidity=65%
```
This shows the system received data from a sensor.

#### Loading a Machine Learning Model
```
INFO com.fireprediction.ml.FireRiskPredictor - Loading existing model
```
The system found a previously trained model and is loading it.

#### Warning
```
WARN com.fireprediction.ml.FireRiskPredictor - Failed to load model, training new one
```
This indicates something wasn't right (couldn't find a model), but the system is recovering by training a new one.

#### Error
```
ERROR com.fireprediction.database.SupabaseDatabase - Failed to connect to database: Connection timeout
```
This shows something went seriously wrong with the database connection.

## How To Use Log Files As A Beginner

### 1. Finding Log Files

Look in the `logs/` folder of your application. You'll see:
- `fire-prediction.log` - the current log file
- `fire-prediction.2025-05-03.log` - yesterday's log file
- And so on...

### 2. Reading Log Files

You can open log files with any text editor (Notepad, VS Code, etc.). They're just plain text files.

### 3. What To Look For

- **Check the timestamps**: Follow events in chronological order
- **Look for ERROR or WARN messages**: These highlight problems
- **Find specific components**: If you're having issues with sensors, look for lines containing "sensor"
- **Track application lifecycle**: Look for startup and shutdown messages

### 4. Common Troubleshooting Using Logs

#### Problem: Application Won't Start
Check the log for ERROR messages during startup. You might see:
```
ERROR com.fireprediction.FirePredictionApplication - Failed to start application: Database connection failed
```

#### Problem: No Sensor Readings
Look for sensor-related messages:
```
ERROR com.fireprediction.sensor.ESP32DHT22Sensor - Cannot connect to sensor on port COM3
```

#### Problem: Incorrect Risk Prediction
Check if the model loaded correctly:
```
WARN com.fireprediction.ml.FireRiskPredictor - Using fallback prediction method
```

### 5. When To Check Logs

- When your application isn't working properly
- When you want to confirm a specific action happened
- When you need to understand what the system is doing
- Before contacting technical support

## How Logging Is Configured in Our System

For those interested in the technical details, our logging is configured in a file called `logback.xml`. Here's what it does:

1. **Sets up two ways to output logs**:
   - To the console (for immediate feedback)
   - To files (for permanent records)

2. **Defines the log format**:
   - Console logs: Simple format with time, thread, level, logger, and message
   - File logs: Detailed format with date, time, thread, level, logger, and message

3. **Configures file rotation**:
   - Creates a new log file each day
   - Keeps 30 days of history

4. **Sets different log levels for different parts of the application**:
   - Our application code (`com.fireprediction`): DEBUG level (very detailed)
   - External libraries: Less detailed (INFO or WARN) to reduce noise

## Benefits of Our Logging System

1. **Comprehensive Recording**: Everything important is captured
2. **Organized Storage**: Logs are neatly organized by date
3. **Automatic Maintenance**: Old logs are cleaned up automatically
4. **Different Detail Levels**: More details for our code, less for external libraries
5. **Multiple Output Targets**: Both console and files for flexibility

## Logging Best Practices For Beginners

If you want to add your own logging to the application:

1. **Get a logger for your class**:
   ```java
   private static final Logger logger = LoggerFactory.getLogger(YourClassName.class);
   ```

2. **Use the appropriate log level**:
   ```java
   logger.debug("Detailed information for debugging");
   logger.info("General information about progress");
   logger.warn("Something unexpected but not critical");
   logger.error("Something went wrong");
   ```

3. **Include relevant information**:
   ```java
   logger.info("Sensor reading: temperature={}°C, humidity={}%", temperature, humidity);
   ```

4. **For errors, include the exception**:
   ```java
   try {
       // Some code that might throw an exception
   } catch (Exception e) {
       logger.error("Failed to process reading: {}", e.getMessage(), e);
   }
   ```

## Conclusion

Logging is an essential part of any application. In our Fire Prediction System, logs provide a detailed record of everything happening in the system. As a beginner, understanding logs helps you:

1. Figure out what went wrong when there's a problem
2. Understand how the application works
3. Track the application's behavior over time
4. Get helpful information when seeking assistance

Remember: When in doubt, check the logs!
