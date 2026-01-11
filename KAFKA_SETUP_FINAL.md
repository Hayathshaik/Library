# Kafka Setup Guide for Windows + WSL

## Your Current Issue
Your Spring Boot application is trying to connect to Kafka at `localhost:9092`, but **Kafka is not running**.

The errors you're seeing:
```
Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.
Bootstrap broker localhost:9092 (id: -1 rack: null) disconnected
```

## ✅ SOLUTION: Start Kafka on Windows

### Option 1: Using Windows with tar (RECOMMENDED)

Run these commands in PowerShell (one at a time):

```powershell
# Step 1: Create Kafka directory
$KafkaHome = "$env:USERPROFILE\kafka"
New-Item -ItemType Directory -Path $KafkaHome -Force | Out-Null

# Step 2: Download Kafka
$ProgressPreference = 'SilentlyContinue'
Invoke-WebRequest -Uri "https://archive.apache.org/dist/kafka/3.6.0/kafka_2.13-3.6.0.tgz" -OutFile "$KafkaHome\kafka.tgz" -UseBasicParsing
Write-Host "✓ Kafka downloaded"

# Step 3: Extract (requires tar - included in Windows 10+)
tar -xzf "$KafkaHome\kafka.tgz" -C $KafkaHome
Remove-Item "$KafkaHome\kafka.tgz"
Write-Host "✓ Kafka extracted"

# Step 4: Start Zookeeper
cd "$KafkaHome\kafka_2.13-3.6.0"
Start-Process -FilePath ".\bin\windows\zookeeper-server-start.bat" `
    -ArgumentList ".\config\zookeeper.properties" -NoNewWindow
Write-Host "✓ Zookeeper started"

# Wait 3 seconds
Start-Sleep -Seconds 3

# Step 5: Start Kafka (in a new window)
Start-Process -FilePath ".\bin\windows\kafka-server-start.bat" `
    -ArgumentList ".\config\server.properties" -NoNewWindow
Write-Host "✓ Kafka started"

# Wait 5 seconds for Kafka to be ready
Start-Sleep -Seconds 5

# Step 6: Create the topic
.\bin\windows\kafka-topics.bat --create --topic book_events `
    --bootstrap-server localhost:9092 `
    --partitions 1 `
    --replication-factor 1 `
    --if-not-exists
Write-Host "✓ Topic 'book_events' created"
```

### Verify Kafka is Running

```powershell
# Check if Kafka is listening on port 9092
netstat -ano | findstr ":9092"
```

You should see output like:
```
TCP    0.0.0.0:9092    0.0.0.0:0    LISTENING    12345
```

---

## 🚀 Start Your Spring Boot Application

Once Kafka is running, start your app:

```bash
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

Or use your IDE to run `LibrarianApplication.java`

---

## ✅ Verify Everything Works

When the app starts, you should see:
1. ✓ No more "Connection to node -1" errors
2. ✓ Kafka connection successful
3. ✓ Outbox dispatcher processes events

---

## Option 2: Using Docker (Alternative)

If you prefer Docker, run:

```powershell
# Make sure Docker Desktop is running
cd C:\Users\Zeelan\Librarian
docker-compose up -d
```

---

## Troubleshooting

### Kafka won't start
- **Ensure Java is installed**: `java -version`
- **Check if port 9092 is in use**: `netstat -ano | findstr ":9092"`
- **Kill existing process**: `taskkill /PID <PID> /F`

### Application can't find Kafka
- Verify Kafka is listening on port 9092
- Check `application.properties` has: `spring.kafka.bootstrap-servers=localhost:9092`
- Restart the application

### Still seeing connection errors
- Wait 10 seconds after starting Kafka before starting the app
- Check Kafka logs in the terminal window where you started it
- Verify firewall isn't blocking port 9092

---

## Configuration Details

Your app is now configured with:
- **Bootstrap Servers**: `localhost:9092`
- **Producer Retries**: 3
- **Message Batching**: Every 10ms or 16KB
- **Connection Timeout**: 5 seconds
- **Request Timeout**: 30 seconds
- **Delivery Timeout**: 120 seconds

These settings ensure reliable message delivery to Kafka!

