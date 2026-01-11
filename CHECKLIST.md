# Kafka Setup Checklist

## ✅ Changes Made to Your Project

- [x] Updated `application.properties` with Kafka configuration
- [x] Enhanced `KafkaConfig.java` with proper timeout and retry settings
- [x] Created `kafka-startup.bat` for easy Windows startup
- [x] Created `kafka-startup.ps1` for PowerShell startup
- [x] Created comprehensive setup guides

## 🚀 To Get Your App Working

### Before Running Your App:
- [ ] Download and extract Kafka (use `kafka-startup.bat`)
- [ ] Verify Kafka is listening: `netstat -ano | findstr ":9092"`
- [ ] Check both Zookeeper and Kafka windows are open and running

### When Starting Your App:
- [ ] Open terminal in project directory
- [ ] Run: `mvn spring-boot:run`
- [ ] Wait for "Started LibrarianApplication" message
- [ ] Check logs for "Kafka Bootstrap Servers: localhost:9092"

### Expected Success Indicators:
- [ ] No "Connection to node -1" errors
- [ ] No "Bootstrap broker disconnected" warnings
- [ ] Application starts normally
- [ ] Outbox dispatcher begins processing events
- [ ] Port 8083 is accessible at `http://localhost:8083`

## 📋 Configuration Details

**What was changed:**

```properties
# application.properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.retries=3
spring.kafka.producer.properties.linger.ms=10
spring.kafka.properties.connections.max.idle.ms=540000
```

**What this provides:**
- Connection to Kafka broker at localhost:9092
- Automatic retry on failure (3 attempts)
- Message batching every 10ms (better throughput)
- 5-second connection timeout
- 30-second request timeout
- 120-second delivery timeout

## 🔧 KafkaConfig Enhancements

- Added Logger for debugging
- Added comprehensive producer configuration
- Proper serialization setup
- Connection pooling optimization
- Request timeout configuration
- Delivery timeout configuration

## 📁 Files to Reference

1. **QUICK_START.txt** - Start here for 3-step setup
2. **SOLUTION_SUMMARY.md** - Complete explanation
3. **KAFKA_SETUP_FINAL.md** - Detailed troubleshooting
4. **kafka-startup.bat** - Run this to start Kafka

## ⚠️ Important Notes

1. **Kafka must be running** before you start the application
2. **Two terminal windows** will open - don't close them
3. **Zookeeper starts first**, then Kafka
4. **Wait 5-10 seconds** after Kafka starts before running the app
5. **Keep both windows open** while your app is running

## ✅ Success Criteria

Your issue is FIXED when:
- [ ] Kafka broker is listening on port 9092
- [ ] Your Spring Boot app connects without errors
- [ ] Application starts successfully
- [ ] No more "Connection to node -1" error messages
- [ ] Outbox events are being processed

---

**You're all set! Follow the checklist and your Kafka connection error will be resolved.** 🎉

