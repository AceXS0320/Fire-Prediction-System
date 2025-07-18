package com.fireprediction.demo;

import com.fireprediction.ml.FireRiskPredictor;
import com.fireprediction.ml.PredictorFactory;
import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.notification.EmailService;
import com.fireprediction.notification.FireRiskNotificationHandler;
import com.fireprediction.sensor.DangerousSimulatedSensor;
import com.fireprediction.sensor.SensorException;
import com.fireprediction.sensor.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration class to showcase the email notification system
 * by generating dangerous temperature readings that trigger alerts.
 */
public class EmailNotificationDemo {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationDemo.class);

    public static void main(String[] args) {
        logger.info("Starting Email Notification Demo");
        
        // 1. Configure email service with your SMTP settings
        // Note: You need to provide valid SMTP settings to send real emails
        EmailService emailService = new EmailService(
                "smtp.gmail.com",           // Email host (e.g., Gmail SMTP server)
                "587",                       // Port (587 for TLS)
                "mustafa.khafaga@gmail.com",      // Your email address
                "pzzz xwis ygmu lyfm",         // Your app password (for Gmail, use app password)
                "briancondon894@gmail.com"      // Where to send the alert
        );
        
        try {
            // 2. Create a notification handler that uses the email service
            FireRiskNotificationHandler notificationHandler = new FireRiskNotificationHandler(emailService);
            
            // 3. Create a sensor manager
            SensorManager sensorManager = new SensorManager(10); // Read every 10 seconds
            
            // 4. Add a dangerous simulated sensor that will generate high temperature readings
            DangerousSimulatedSensor highRiskSensor = new DangerousSimulatedSensor(
                    "high-risk-sensor",
                    "Server Room A",
                    false  // false = HIGH risk (41-50°C), true = EXTREME risk (51-60°C)
            );
            
            DangerousSimulatedSensor extremeRiskSensor = new DangerousSimulatedSensor(
                    "extreme-risk-sensor",
                    "Server Room B",
                    true   // Generate extreme risk temperatures
            );
            
            // 5. Initialize sensors
            sensorManager.addSensor(highRiskSensor);
            sensorManager.addSensor(extremeRiskSensor);
            sensorManager.initializeSensors();
            
            // 6. Register the notification handler to receive sensor readings
            sensorManager.addReadingListener(notificationHandler);
            
            // 7. Start the sensor readings
            logger.info("Starting scheduled sensor readings. Check your inbox for alerts!");
            logger.info("Press Ctrl+C to exit the demo");
            
            sensorManager.startScheduledReadings();
            
            // Keep the program running for a while
            Thread.sleep(5 * 60 * 1000); // Run for 5 minutes
            
        } catch (SensorException e) {
            logger.error("Error during sensor setup: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.info("Demo interrupted");
        }
    }
    
    /**
     * Simple demonstration of how the risk levels work.
     * This method shows how different temperatures map to risk levels.
     */
    private static void demonstrateRiskLevels() {
        double[] temperatures = {25.0, 35.0, 45.0, 55.0};
        
        for (double temperature : temperatures) {
            FireRiskLevel riskLevel = FireRiskLevel.fromTemperature(temperature);
            logger.info("Temperature: {}°C => Risk Level: {} ({})", 
                    temperature, riskLevel, riskLevel.getDescription());
            
            if (riskLevel.requiresImmediateAction()) {
                logger.warn("Temperature {}°C requires immediate action!", temperature);
            }
        }
    }
}
