# Object-Oriented Programming (OOP) in the Fire Prediction System
This guide explains the key Object-Oriented Programming (OOP) principles used in our Fire Prediction System. It's designed for absolute beginners to understand how the code is structured and how different components work together.

## Key OOP Concepts Used in Our Project

### 1. Classes and Objects


**Where we used it:**
- `EmailService` class: A blueprint for email functionality
- `SensorReading` class: A blueprint for storing sensor data
- When we create a new instance: `EmailService emailService = new EmailService(...)`

### 2. Encapsulation

**What is it?**
- Hiding internal details and providing a controlled interface
- Achieved through private attributes and public methods
- Protects data from unwanted external modifications

**Where we used it:**
- Private fields in `EmailService` (host, port, etc.)
- Getter/setter methods that control access to data
- Package-private methods that restrict access to certain classes


### 3. Inheritance

**What is it?**
- Creating new classes that inherit properties and methods from existing classes
- The "is-a" relationship (e.g., a DangerousSimulatedSensor "is-a" SimulatedSensor)
- Promotes code reuse

**Where we used it:**
- `DangerousSimulatedSensor` extends `SimulatedSensor`
- Inherits all the behavior but adds specialized temperature generation

```java
// Parent class
public class SimulatedSensor implements Sensor {
    protected String sensorId;
    protected String location;
    
    public SensorReading readSensor() {
        // Base implementation
    }
}

// Child class - inherits from SimulatedSensor
public class DangerousSimulatedSensor extends SimulatedSensor {
    private boolean extremeRisk;
    
    @Override
    public SensorReading readSensor() {
        // Specialized implementation that generates dangerous temperature readings
    }
}
```

### 4. Polymorphism

**What is it?**
- The ability of objects to take different forms depending on the context
- Method overriding (same method, different implementation in subclasses)
- Method overloading (same method name, different parameters)

**Where we used it:**
- The `Sensor` interface with different implementations
- The `readSensor()` method behaves differently in each sensor type
- Method overloading in `ErrorHandler` for different error scenarios

```java
// Interface defining the behavior
public interface Sensor {
    SensorReading readSensor() throws SensorException;
}

// Different implementations of the same interface
public class ESP32DHT22Sensor implements Sensor {
    @Override
    public SensorReading readSensor() {
        // Implementation for real hardware
    }
}

public class SimulatedSensor implements Sensor {
    @Override
    public SensorReading readSensor() {
        // Implementation for simulated data
    }
}

// SensorManager can work with any type of Sensor
sensorManager.addSensor(new ESP32DHT22Sensor("ESP1", "COM3", "Living Room"));
sensorManager.addSensor(new SimulatedSensor("SIM1", "Kitchen"));
sensorManager.addSensor(new DangerousSimulatedSensor("DANGER1", "Server Room", true));
```

### 5. Abstraction

**What is it?**
- Hiding complex implementation details and showing only necessary features
- Focusing on what an object does instead of how it does it
- Achieved through interfaces and abstract classes

**Where we used it:**
- The `Sensor` interface abstracts the details of different sensor types
- The `Database` interface hides the complexity of database operations
- The `FireRiskPredictor` interface abstracts machine learning details

```java
// Abstract interface
public interface Database {
    void initialize() throws DatabaseException;
    void saveSensorReading(SensorReading reading) throws DatabaseException;
    List<SensorReading> getAllSensorReadings() throws DatabaseException;
    // Other methods...
}

// Concrete implementation
public class SupabaseDatabase implements Database {
    // Implementation details hidden from users of the Database interface
}
```

## Design Patterns Used

### 1. Observer Pattern

**What is it?**
- A way for objects to notify other objects about changes
- "Publisher-Subscriber" relationship

**Where we used it:**
- `SensorManager` (publisher) notifies listeners (subscribers) about new readings
- `FireRiskNotificationHandler` subscribes to receive sensor readings

```java
// Publisher
public class SensorManager {
    private List<Consumer<SensorReading>> readingListeners = new ArrayList<>();
    
    public void addReadingListener(Consumer<SensorReading> listener) {
        readingListeners.add(listener);
    }
    
    private void notifyListeners(SensorReading reading) {
        for (Consumer<SensorReading> listener : readingListeners) {
            listener.accept(reading);
        }
    }
}

// Subscriber
public class FireRiskNotificationHandler implements Consumer<SensorReading> {
    @Override
    public void accept(SensorReading reading) {
        // Process the reading and send notifications if needed
    }
}

// Connecting publisher and subscriber
sensorManager.addReadingListener(notificationHandler);
```

### 2. Factory Pattern

**What is it?**
- Centralizes object creation logic
- Creates objects without exposing creation logic

**Where we used it:**
- `PredictorFactory` creates different types of predictors

```java
public class PredictorFactory {
    public static FireRiskPredictor createAndInitialize(String predictorType) {
        FireRiskPredictor predictor;
        
        switch (predictorType) {
            case "SMILE":
                predictor = new SmileFireRiskPredictor();
                break;
            default:
                predictor = new DummyFireRiskPredictor();
        }
        
        return predictor;
    }
}
```

### 3. Singleton Pattern

**What is it?**
- Ensures a class has only one instance
- Provides a global point of access to that instance

**Where we used it:**
- `AppConfig` is a singleton to ensure consistent configuration

```java
public class AppConfig {
    private static AppConfig instance;
    
    private AppConfig() {
        // Private constructor prevents direct instantiation
    }
    
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
}
```

## How Classes Communicate

### 1. The Email Notification Flow

Here's how the email notification system works:

1. **SensorManager** reads data from sensors at regular intervals
2. When new data arrives, **SensorManager** notifies all listeners (Observer pattern)
3. **FireRiskNotificationHandler** (a listener) receives the reading
4. **FireRiskNotificationHandler** determines if the reading indicates a dangerous situation
5. If dangerous, **FireRiskNotificationHandler** calls **EmailService**
6. **EmailService** composes and sends an email notification

```
SensorManager → FireRiskNotificationHandler → EmailService → Email sent
```

### 2. Dependency Injection

Many classes in our system use **Dependency Injection** - a technique where objects receive their dependencies from outside rather than creating them internally.

```java
// Instead of this (tight coupling):
public class FireRiskNotificationHandler {
    private EmailService emailService = new EmailService(...);  // Created internally
}

// We do this (dependency injection):
public class FireRiskNotificationHandler {
    private final EmailService emailService;  // Injected from outside
    
    public FireRiskNotificationHandler(EmailService emailService) {
        this.emailService = emailService;
    }
}

// Usage:
EmailService emailService = new EmailService(...);
FireRiskNotificationHandler handler = new FireRiskNotificationHandler(emailService);
```

Benefits:
- More flexible and easier to test
- Can swap implementations without changing the dependent class
- Makes dependencies explicit

## Why We Used These OOP Principles

1. **Maintainability**: Classes with single responsibilities are easier to maintain
2. **Reusability**: Inheritance and interfaces promote code reuse
3. **Flexibility**: Polymorphism allows for easy extension
4. **Testability**: Dependency injection makes testing easier
5. **Scalability**: The system can grow with new sensor types or notification methods

## Practical Example: Adding a New Notification Method

The OOP principles make it easy to extend our system. For example, to add SMS notifications:

1. Create a new `SmsService` class
2. Create a `NotificationService` interface that both `EmailService` and `SmsService` implement
3. Modify `FireRiskNotificationHandler` to accept any `NotificationService`

The system would still work the same way, but with the flexibility to use different notification methods!

## Conclusion

Our Fire Prediction System demonstrates key OOP principles that make the code more organized, reusable, and maintainable. By understanding these principles, you'll be better equipped to explain the design decisions in your presentation and to extend the system in the future.
