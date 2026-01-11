# Kafka Setup Guide for Windows + WSL

## Problem
Your Spring Boot application tries to connect to Kafka on `localhost:9092`, but **Kafka is not running**.

## Solution: Run Kafka in WSL

### Step 1: Start Kafka in WSL

Run this command in PowerShell:

```powershell
wsl bash -c "bash /mnt/c/Users/Zeelan/Librarian/kafka-startup.sh"
```

This script will:
- Download Kafka 3.6.0 (if not already installed)
- Start Zookeeper
- Start Kafka broker
- Create the `book_events` topic
- Verify Kafka is listening on port 9092

**Expected output:**
```
Kafka is ready!
```

### Step 2: Verify Kafka is Running

```powershell
wsl bash -c "netstat -tulpn | grep 9092"
```

You should see:
```
tcp  0  0  0.0.0.0:9092  0.0.0.0:*  LISTEN  <PID>/java
```

### Step 3: Start Your Spring Boot Application

```powershell
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

or via IDE: Run the Spring Boot application

### Step 4: Verify Connection

Once the app starts, you should **NOT** see these warnings anymore:
```
Bootstrap broker localhost:9092 (id: -1 rack: null) disconnected
Connection to node -1 (localhost/127.0.0.1:9092) could not be established
```

Instead, you'll see successful connection messages and the outbox dispatcher will process events.

---

## Alternative: Run Kafka on Windows WSL IP

If you're running the app on Windows but want Kafka in WSL:

### 1. Get WSL IP Address

```powershell
wsl bash -c "hostname -I"
```

Example output: `172.31.47.42`

### 2. Start Kafka in WSL with Export

```powershell
wsl bash -c "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.31.47.42:9092 bash /mnt/c/Users/Zeelan/Librarian/kafka-startup.sh"
```

### 3. Update application.properties

```properties
spring.kafka.bootstrap-servers=172.31.47.42:9092
```

---

## Troubleshooting

### Check if Kafka is running
```powershell
# In WSL
wsl bash -c "netstat -tulpn | grep 9092"

# Or check processes
wsl bash -c "ps aux | grep kafka"
```

### View Kafka logs
```powershell
wsl bash -c "tail -f /tmp/kafka.log"
```

### View Zookeeper logs
```powershell
wsl bash -c "tail -f /tmp/zookeeper.log"
```

### Stop Kafka & Zookeeper
```powershell
wsl bash -c "pkill -f kafka-server-start; pkill -f zookeeper-server-start"
```

### Reset Kafka (delete all data)
```powershell
wsl bash -c "rm -rf ~/kafka/kafka_2.13-3.6.0/data-logs/*"
```

---

## Configuration Summary

Your application is now configured with:
- **Bootstrap Servers**: `localhost:9092`
- **Producer Retries**: 3
- **Linger Time**: 10ms
- **Connection Timeout**: 5000ms
- **Idle Connection Timeout**: 540000ms (9 minutes)

These settings allow the application to:
1. ✓ Start even if Kafka is temporarily unavailable
2. ✓ Retry failed messages up to 3 times
3. ✓ Batch messages for better throughput

