package com.fireprediction.notification;

import com.fireprediction.model.FireRiskLevel;
import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Service for sending email notifications when dangerous fire conditions are detected.
 */
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final String emailHost;
    private final String emailPort;
    private final String senderEmail;
    private final String senderPassword;
    private final String recipientEmail;
    
    /**
     * Create a new email service with specified email configuration.
     * 
     * @param emailHost SMTP server host (e.g., "smtp.gmail.com")
     * @param emailPort SMTP server port (e.g., "587" for TLS)
     * @param senderEmail Email address to send from
     * @param senderPassword Password for the sender email
     * @param recipientEmail Email address to send notifications to
     */
    public EmailService(String emailHost, String emailPort, String senderEmail, 
                        String senderPassword, String recipientEmail) {
        this.emailHost = emailHost;
        this.emailPort = emailPort;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        this.recipientEmail = recipientEmail;
    }
    
    /**
     * Send a fire risk notification email.
     * 
     * @param reading The sensor reading that triggered the notification
     * @param riskLevel The detected risk level
     * @return true if the email was sent successfully
     */
    public boolean sendFireRiskNotification(SensorReading reading, FireRiskLevel riskLevel) {
        logger.info("Preparing to send fire risk notification email for risk level: {}", riskLevel);
        
        // Set up mail properties
        Properties props = new Properties();
        props.put("mail.smtp.host", emailHost);
        props.put("mail.smtp.port", emailPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        try {
            // Create a mail session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
            
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            
            // Set email subject based on risk level
            message.setSubject("[FIRE ALERT] " + riskLevel + " Fire Risk Detected");
            
            // Create the email content
            String emailContent = createEmailContent(reading, riskLevel);
            message.setContent(emailContent, "text/html; charset=utf-8");
            
            // Send the email
            Transport.send(message);
            
            logger.info("Successfully sent fire risk notification email to {}", recipientEmail);
            return true;
            
        } catch (MessagingException e) {
            logger.error("Failed to send email notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create the HTML content for the notification email.
     * 
     * @param reading The sensor reading that triggered the notification
     * @param riskLevel The detected risk level
     * @return HTML content for the email
     */
    private String createEmailContent(SensorReading reading, FireRiskLevel riskLevel) {
        StringBuilder content = new StringBuilder();
        
        content.append("<html><body>");
        content.append("<h2 style='color: ").append(getColorForRiskLevel(riskLevel)).append(";'>")
               .append(riskLevel).append(" Fire Risk Alert</h2>");
        
        content.append("<p><strong>A ").append(riskLevel)
               .append(" fire risk has been detected at location: </strong>")
               .append(reading.getLocation()).append("</p>");
        
        content.append("<h3>Sensor Reading Details:</h3>");
        content.append("<ul>");
        content.append("<li><strong>Sensor ID:</strong> ").append(reading.getSensorId()).append("</li>");
        content.append("<li><strong>Temperature:</strong> ").append(String.format("%.1f", reading.getTemperature()))
               .append("°C</li>");
        content.append("<li><strong>Humidity:</strong> ").append(String.format("%.1f", reading.getHumidity()))
               .append("%</li>");
        content.append("<li><strong>Timestamp:</strong> ").append(reading.getTimestamp()).append("</li>");
        content.append("</ul>");
        
        content.append("<h3>Recommended Action:</h3>");
        content.append("<p>").append(riskLevel.getRecommendedAction()).append("</p>");
        
        content.append("<p>This is an automated alert from the Fire Prediction System.</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    /**
     * Get CSS color string for the risk level.
     * 
     * @param riskLevel The risk level
     * @return CSS color string
     */
    private String getColorForRiskLevel(FireRiskLevel riskLevel) {
        switch (riskLevel) {
            case LOW:
                return "#4CAF50"; // Green
            case MODERATE:
                return "#FFC107"; // Yellow
            case HIGH:
                return "#FF9800"; // Orange
            case EXTREME:
                return "#F44336"; // Red
            default:
                return "#000000"; // Black
        }
    }
}
