package com.fireprediction.notification;

import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Handles fire risk notifications by analyzing sensor readings
 * and sending email alerts when dangerous fire conditions are detected.
 */
public class FireRiskNotificationHandler implements Consumer<SensorReading> {
    private static final Logger logger = LoggerFactory.getLogger(FireRiskNotificationHandler.class);
    
    private final EmailService emailService;
    private boolean notificationSent = false;
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_COOLDOWN_MS = 15 * 60 * 1000; // 15 minutes
    
    /**
     * Creates a fire risk notification handler with the given email service.
     * 
     * @param emailService The email service to use for sending notifications
     */
    public FireRiskNotificationHandler(EmailService emailService) {
        this.emailService = emailService;
        logger.info("Fire Risk Notification Handler initialized");
    }
    
    /**
     * Process a sensor reading and send notifications if dangerous conditions are detected.
     * 
     * @param reading The sensor reading to process
     */
    @Override
    public void accept(SensorReading reading) {
        // Calculate fire risk level based on temperature
        FireRiskLevel riskLevel = FireRiskLevel.fromTemperature(reading.getTemperature());
        
        // Check if this risk level requires immediate action and if we're not in cooldown
        if (riskLevel.requiresImmediateAction() && canSendNotification()) {
            logger.warn("ALERT: {} fire risk detected from sensor {}! Temperature: {}°C, Humidity: {}%",
                    riskLevel, reading.getSensorId(), 
                    String.format("%.1f", reading.getTemperature()),
                    String.format("%.1f", reading.getHumidity()));
            
            // Send email notification
            boolean emailSent = emailService.sendFireRiskNotification(reading, riskLevel);
            
            if (emailSent) {
                logger.info("Email notification sent for {} fire risk", riskLevel);
                notificationSent = true;
                lastNotificationTime = System.currentTimeMillis();
            } else {
                logger.error("Failed to send email notification for {} fire risk", riskLevel);
            }
        } else if (riskLevel.requiresImmediateAction()) {
            logger.info("High risk detected but notification in cooldown period. Skipping email.");
        }
    }
    
    /**
     * Check if we can send a notification based on cooldown period.
     * 
     * @return true if notification can be sent
     */
    private boolean canSendNotification() {
        if (!notificationSent) {
            return true; // First notification can always be sent
        }
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastNotification = currentTime - lastNotificationTime;
        
        return timeSinceLastNotification > NOTIFICATION_COOLDOWN_MS;
    }
    
    /**
     * Reset the notification state.
     */
    public void resetNotificationState() {
        notificationSent = false;
    }
}
