# ✅ KAFKA SETUP - MANUAL COMMANDS FOR WINDOWS

## Status
- ✅ Kafka is downloaded and extracted to: `C:\Users\Zeelan\kafka_direct\kafka_2.13-3.6.0`
- ✅ Java processes are running

## To Start Kafka Properly (Run These Commands)

### Step 1: Open First PowerShell Window - Start Zookeeper

Copy and paste this command:
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"; .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```

**Wait until you see messages about Zookeeper binding to port 2181**

### Step 2: Open Second PowerShell Window - Start Kafka Broker

Copy and paste this command:
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"; .\bin\windows\kafka-server-start.bat .\config\server.properties
```

**Wait until you see messages about Kafka broker starting**

### Step 3: Create Topic (Open Third PowerShell Window)

Copy and paste this command:
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"; .\bin\windows\kafka-topics.bat --create --topic book_events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists
```

### Step 4: Verify Kafka is Running

In a fourth PowerShell window, run:
```powershell
netstat -ano | Select-String ":9092"
```

Should show a listening port.

### Step 5: Start Your Spring Boot App

In a fifth PowerShell window:
```powershell
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

---

## OR Use This Single Command (Opens 2 Windows Automatically)

Copy and paste in ONE PowerShell window:

```powershell
$KafkaDir = "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"; Start-Process -FilePath "powershell" -ArgumentList "-NoExit -Command `"cd '$KafkaDir'; .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties`""; Start-Sleep -Seconds 3; Start-Process -FilePath "powershell" -ArgumentList "-NoExit -Command `"cd '$KafkaDir'; .\bin\windows\kafka-server-start.bat .\config\server.properties`""; Start-Sleep -Seconds 5; Write-Host "Kafka and Zookeeper started in separate windows!"; Start-Sleep -Seconds 2; & "$KafkaDir\bin\windows\kafka-topics.bat" --create --topic book_events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists
```

---

## Quick Troubleshooting

**Nothing is starting?**
- Check Java is installed: `java -version`
- Check port 9092 is free: `netstat -ano | Select-String ":9092"`

**Kafka starts but closes immediately?**
- Check Zookeeper is running first
- Look for error messages in the terminal window

**Still can't connect?**
- Ensure both Kafka and Zookeeper windows are open and showing no errors
- Wait 10 seconds after both start before running your app

---

## Next: Start Your App

Once Kafka is running:
```powershell
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

