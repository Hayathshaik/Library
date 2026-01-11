# ✅ KAFKA SETUP - FINAL STEP-BY-STEP GUIDE

## Current Status
✅ Kafka is installed at: `C:\Users\Zeelan\kafka_direct\kafka_2.13-3.6.0`
✅ All configuration files are in place

---

## 🚀 EASIEST METHOD - ONE COMMAND (RECOMMENDED)

### Just run this in PowerShell (as Administrator):

```powershell
cd C:\Users\Zeelan\Librarian; .\START-KAFKA.ps1
```

**That's it!** This script will:
- ✓ Start Zookeeper in a new window
- ✓ Start Kafka in a new window
- ✓ Create the `book_events` topic
- ✓ Verify everything is working

Then run your app:
```powershell
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

---

## OR: Manual Step-by-Step Method

If the automated script doesn't work, follow these manual steps:

## How to Start Kafka Manually (GUARANTEED TO WORK)

### Step 1: Open PowerShell as Administrator

Right-click on PowerShell → Run as Administrator

### Step 2: Navigate to Kafka Directory

Copy and paste this command:
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
```

### Step 3: Start Zookeeper

In the same PowerShell window, run:
```powershell
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```

**Wait for this output:**
```
[main] INFO org.apache.zookeeper.server.quorum.QuorumPeerMain - Using config file: ...
```

**Important: Keep this window open!**

### Step 4: Open Second PowerShell Window

Open another PowerShell window (Admin)

Navigate to Kafka:
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
```

### Step 5: Start Kafka Broker

```powershell
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

**Wait for this output:**
```
[main] INFO org.apache.kafka.server.KafkaServer - [KafkaServer id=1] started successfully in ...
```

**Important: Keep this window open!**

### Step 6: Verify Kafka is Running

Open a third PowerShell window:
```powershell
netstat -ano | findstr "9092"
```

Should show:
```
TCP    0.0.0.0:9092    0.0.0.0:0    LISTENING    <PID>
```

### Step 7: Create Topic (Optional but Recommended)

In the third PowerShell window:
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
.\bin\windows\kafka-topics.bat --create --topic book_events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists
```

---

## NOW: Start Your Spring Boot Application

Open fourth PowerShell window:
```powershell
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

---

## ✅ Success Indicators

When everything is working:

1. **Zookeeper window** shows:
   ```
   Zookeeper server started on port 2181
   ```

2. **Kafka window** shows:
   ```
   [KafkaServer id=1] started successfully in ...
   ```

3. **Your app window** shows:
   ```
   Started LibrarianApplication
   ```

4. **NO errors** like "Connection to node -1"

---

## 🆘 Troubleshooting

### If Zookeeper or Kafka won't start:
1. Ensure you're running as Administrator
2. Check if Java is installed: `java -version`
3. Try deleting temp files: 
   ```powershell
   rm -Force -Recurse "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0\logs" -ErrorAction SilentlyContinue
   ```
4. Restart Zookeeper and Kafka

### If port 9092 is still not showing:
1. Kill all Java processes:
   ```powershell
   taskkill /IM java.exe /F
   ```
2. Wait 5 seconds
3. Start again from Step 3

### If your app still can't connect:
1. Verify both Zookeeper and Kafka windows show no errors
2. Wait 15 seconds after Kafka starts
3. Check `application.properties` has:
   ```
   spring.kafka.bootstrap-servers=localhost:9092
   ```
4. Restart your app

---

## Quick Command Summary

```powershell
# Window 1 - Zookeeper
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# Window 2 - Kafka (start after Zookeeper shows "started")
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
.\bin\windows\kafka-server-start.bat .\config\server.properties

# Window 3 - Verify (after Kafka shows "started successfully")
netstat -ano | findstr "9092"

# Window 4 - Your App (wait 10 seconds, then run)
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

---

## ✨ That's it!

Follow these exact steps and your Kafka connection error will be resolved. Your Spring Boot application will successfully connect to Kafka at `localhost:9092`.

**Do NOT close the Zookeeper or Kafka windows while your application is running.**

