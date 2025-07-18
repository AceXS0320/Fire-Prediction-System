package com.fireprediction.sensor;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import com.fireprediction.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the Sensor interface for an ESP32 with DHT22 temperature and humidity sensor.
 * Communicates via serial port with an ESP32 microcontroller that has a DHT22 sensor attached.
 * 
 * OOP Principles:
 * - Polymorphism: Implements Sensor interface
 * - Encapsulation: Encapsulates serial communication details
 * - Single Responsibility: Handles only sensor communication
 */
public class ESP32DHT22Sensor implements Sensor {

    private static final Logger logger = LoggerFactory.getLogger(ESP32DHT22Sensor.class);
    
    private static final String DELIMITER = "\n";
    private static final byte[] DELIMITER_BYTES = DELIMITER.getBytes(StandardCharsets.UTF_8);
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = SerialPort.ONE_STOP_BIT;
    private static final int PARITY = SerialPort.NO_PARITY;
    private static final int READ_TIMEOUT = 2000;
    
    private final String sensorId;
    private final String location;
    private final String portName;
    private SerialPort serialPort;
    private boolean connected = false;
    
    // Queue for readings received from sensor
    private final BlockingQueue<SensorReading> readingQueue = new LinkedBlockingQueue<>();
    
    /**
     * Create an ESP32DHT22Sensor with the specified ID and port.
     * 
     * @param sensorId Unique identifier for this sensor
     * @param portName Serial port name to connect to (e.g., "COM3" on Windows)
     * @param location Physical location description
     */
    public ESP32DHT22Sensor(String sensorId, String portName, String location) {
        this.sensorId = sensorId;
        this.portName = portName;
        this.location = location;
        logger.debug("Created ESP32DHT22Sensor with ID: {}, port: {}, location: {}", 
                sensorId, portName, location);
    }
    
    /**
     * Initialize the sensor connection.
     * 
     * @throws SensorException if initialization fails
     */
    @Override
    public void initialize() throws SensorException {
        logger.info("Initializing ESP32DHT22Sensor on port {}", portName);
        
        try {
            // Find the serial port
            serialPort = SerialPort.getCommPort(portName);
            
            // Configure serial port
            serialPort.setComPortParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, READ_TIMEOUT, 0);
            
            // Try to open port
            if (!serialPort.openPort()) {
                throw new SensorException("Failed to open serial port: " + portName);
            }
            
            // Register message listener
            serialPort.addDataListener(new ESP32MessageListener());
            
            connected = true;
            logger.info("ESP32DHT22Sensor initialized on port {}", portName);
            
        } catch (Exception e) {
            connected = false;
            throw new SensorException("Failed to initialize ESP32DHT22Sensor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Request a reading from the sensor.
     * 
     * @return SensorReading with temperature and humidity data
     * @throws SensorException if reading fails
     */
    @Override
    public SensorReading readData() throws SensorException {
        if (!connected) {
            throw new SensorException("Sensor not connected");
        }
        
        try {
            logger.debug("Requesting reading from ESP32DHT22Sensor");
            
            // Send command to ESP32 to take a reading
            byte[] command = "READ\n".getBytes(StandardCharsets.UTF_8);
            serialPort.writeBytes(command, command.length);
            
            // Wait for response (with timeout)
            SensorReading reading = readingQueue.poll(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            if (reading == null) {
                throw new SensorException("Timeout waiting for sensor reading");
            }
            
            logger.debug("Received reading: {}°C, {}%", 
                    String.format("%.1f", reading.getTemperature()), 
                    String.format("%.1f", reading.getHumidity()));
            
            return reading;
            
        } catch (Exception e) {
            throw new SensorException("Failed to read from ESP32DHT22Sensor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if the sensor is connected.
     * 
     * @return true if connected
     */
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Get the sensor ID.
     * 
     * @return Sensor ID
     */
    @Override
    public String getSensorId() {
        return sensorId;
    }
    
    /**
     * Get the location of this sensor.
     * 
     * @return Location description
     */
    @Override
    public String getLocation() {
        return location;
    }
    
    /**
     * Close the sensor connection.
     * 
     * @throws SensorException if closing fails
     */
    @Override
    public void close() throws SensorException {
        if (connected && serialPort != null) {
            try {
                serialPort.removeDataListener();
                serialPort.closePort();
                connected = false;
                logger.info("ESP32DHT22Sensor connection closed");
            } catch (Exception e) {
                throw new SensorException("Error closing ESP32DHT22Sensor: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Serial port message listener for ESP32 communication.
     */
    private class ESP32MessageListener implements SerialPortMessageListener {
        
        @Override
        public byte[] getMessageDelimiter() {
            return DELIMITER_BYTES;
        }
        
        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return true;
        }
        
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }
        
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                return;
            }
            
            try {
                // Get message data
                byte[] data = event.getReceivedData();
                String message = new String(data, StandardCharsets.UTF_8).trim();
                
                logger.debug("Received message from ESP32: {}", message);
                
                // Parse message
                if (message.startsWith("READING:")) {
                    String[] parts = message.substring(8).split(",");
                    if (parts.length == 2) {
                        try {
                            double temperature = Double.parseDouble(parts[0].trim());
                            double humidity = Double.parseDouble(parts[1].trim());
                            
                            SensorReading reading = SensorReading.create(sensorId, temperature, humidity, location);
                            readingQueue.offer(reading);
                            
                            logger.debug("Parsed reading: {}°C, {}%", temperature, humidity);
                        } catch (NumberFormatException e) {
                            logger.error("Invalid number format in sensor data: {}", message, e);
                        }
                    } else {
                        logger.error("Invalid data format from sensor: {}", message);
                    }
                } else if (message.startsWith("ERROR:")) {
                    logger.error("Sensor error: {}", message.substring(6).trim());
                } else {
                    logger.debug("Unrecognized message from sensor: {}", message);
                }
                
            } catch (Exception e) {
                logger.error("Error processing sensor data: {}", e.getMessage(), e);
            }
        }
    }
}
