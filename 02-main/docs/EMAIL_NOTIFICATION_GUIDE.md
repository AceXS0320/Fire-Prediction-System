# Email Notification System for Fire Prediction
## A Guide to Testing High-Risk Conditions

This guide explains how to use the newly added components to test the email notification system with simulated dangerous temperature readings.

## What I've Added to Your Project

1. **DangerousSimulatedSensor**: A special sensor that generates high temperature readings to trigger alerts
2. **Email Notification System**: Components for sending email alerts when dangerous conditions are detected
3. **Demo Application**: A standalone class to test email notifications

## How to Test the Email Notification System

### Step 1: Configure the Email Settings

Open the `EmailNotificationDemo.java` file and update the email configuration with your real SMTP settings:

```java
EmailService emailService = new EmailService(
    "smtp.gmail.com",           // Your email provider's SMTP server
    "587",                       // SMTP port (usually 587 for TLS)
    "your-email@gmail.com",      // Your email address
    "your-app-password",         // Your email password or app password
    "recipient@example.com"      // Where to send the alerts
);
```

**Notes for Gmail Users**:
- You'll need to use an "App Password" instead of your regular password
- To create an App Password:
  1. Go to your Google Account → Security
  2. Enable 2-Step Verification if not already enabled
  3. Go to "App passwords" and generate a new one for this application

### Step 2: Run the Demo Application

There are two ways to test the email notification system:

#### Option 1: Run the Demo Class Directly

```
mvn compile exec:java -Dexec.mainClass="com.fireprediction.demo.EmailNotificationDemo"
```

This will:
1. Start two simulated sensors that generate dangerous temperature readings
2. Process these readings through the notification system
3. Send email alerts when high-risk conditions are detected

#### Option 2: Integrate into the Main Application

To add email notifications to the main Fire Prediction application:

1. Create the email service and notification handler:

```java
// In FirePredictionApplication.java, inside the initializeComponents() method:

// Create email service
EmailService emailService = new EmailService(
    "smtp.gmail.com", 
    "587", 
    "your-email@gmail.com",
    "your-app-password", 
    "recipient@example.com"
);

// Create notification handler
FireRiskNotificationHandler notificationHandler = new FireRiskNotificationHandler(emailService);

// Register the notification handler with the sensor manager
sensorManager.addReadingListener(notificationHandler);
```

2. Replace a regular simulated sensor with the dangerous one:

```java
// Instead of creating a normal simulated sensor:
// sensorManager.addSensor(new SimulatedSensor("sensor-1", "Living Room"));

// Create a dangerous one to trigger alerts:
DangerousSimulatedSensor dangerousSensor = new DangerousSimulatedSensor(
    "dangerous-sensor-1",
    "Server Room",
    true  // true for EXTREME risk (51-60°C), false for HIGH risk (41-50°C)
);
sensorManager.addSensor(dangerousSensor);
```

## Understanding the Risk Levels

The system has four risk levels defined in `FireRiskLevel.java`:

1. **LOW** (0-30°C): Normal conditions, no action required
2. **MODERATE** (31-40°C): Elevated risk, maintain awareness
3. **HIGH** (41-50°C): Significant risk, implement fire prevention measures
4. **EXTREME** (51-100°C): Dangerous conditions, evacuate immediately

The notification system will send email alerts when either **HIGH** or **EXTREME** risk levels are detected, as determined by the `requiresImmediateAction()` method.

## How the Email Notification System Works

The system works in three main steps:

1. **Sensor Readings**: The `DangerousSimulatedSensor` generates high temperature readings (41-60°C)

2. **Risk Assessment**: These readings are converted to risk levels using `FireRiskLevel.fromTemperature()`

3. **Notification**: The `FireRiskNotificationHandler` detects high-risk conditions and triggers email alerts

To prevent flooding your inbox, the notification handler has a cooldown period (15 minutes by default) before sending another alert for the same risk level.

## Customizing the System

### Changing the Cooldown Period

If you want to receive alerts more or less frequently, you can modify the cooldown period in `FireRiskNotificationHandler.java`:

```java
// Change this line to adjust the cooldown period (in milliseconds)
private static final long NOTIFICATION_COOLDOWN_MS = 15 * 60 * 1000; // 15 minutes
```

### Changing Temperature Thresholds

If you want to adjust what temperatures trigger alerts, you can modify the `FireRiskLevel.java` enum:

```java
// These lines define the temperature ranges for each risk level
LOW(0, 30, "Low fire risk", "No action required", Color.GREEN),
MODERATE(31, 40, "Moderate fire risk", "Maintain awareness", Color.YELLOW),
HIGH(41, 50, "High fire risk", "Implement fire prevention measures", Color.ORANGE),
EXTREME(51, 100, "Extreme fire risk", "Evacuate immediately", Color.RED);
```

## Troubleshooting

### "Authentication failed" Errors

If you see authentication errors in the logs:
- Check that your email and password are correct
- For Gmail, make sure you're using an App Password, not your regular password
- Confirm that "Less secure app access" is enabled in your email account (if applicable)

### No Emails Being Sent

If the logs show "Preparing to send fire risk notification" but no emails arrive:
- Check your spam folder
- Verify your SMTP settings (server, port)
- Some email providers block automated emails - try using a different email provider

### Connection Errors

If you see connection errors:
- Check your internet connection
- Verify the SMTP server address is correct
- Ensure your firewall isn't blocking outgoing SMTP connections

## Next Steps for Development

To further enhance the email notification system, consider:

1. Making email settings configurable via environment variables
2. Adding SMS notifications via a service like Twilio
3. Creating a dashboard to view notification history
4. Implementing a more sophisticated risk assessment algorithm
