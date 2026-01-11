# 🚀 START KAFKA - SIMPLE GUIDE

## ✅ Quick Fix for Your Error

Your error: `Connection to node -1 (localhost/127.0.0.1:9092) could not be established`

**Root cause:** Kafka is not running

---

## 📝 SOLUTION - Run These 3 Commands

### Option A: Automated (Easiest)

Open **PowerShell as Administrator** and run:

```powershell
cd C:\Users\Zeelan\Librarian
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
.\START-KAFKA.ps1
```

Wait for the script to finish, then run your app:
```powershell
mvn spring-boot:run
```

---

### Option B: Manual (If Option A doesn't work)

**Window 1 - Start Zookeeper:**
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```
Keep this window open!

**Window 2 - Start Kafka (wait 5 seconds after Window 1):**
```powershell
cd "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"
.\bin\windows\kafka-server-start.bat .\config\server.properties
```
Keep this window open!

**Window 3 - Verify (wait 10 seconds after Window 2):**
```powershell
netstat -ano | findstr "9092"
```
Should show: `LISTENING`

**Window 4 - Start Your App:**
```powershell
cd C:\Users\Zeelan\Librarian
mvn spring-boot:run
```

---

## ✅ When It Works

You'll see:
- ✓ No "Connection to node -1" errors
- ✓ App logs show: `Kafka Bootstrap Servers: localhost:9092`
- ✓ Application starts successfully

---

## 🆘 Still Not Working?

### Quick Fix:
```powershell
# Kill all Java processes
taskkill /IM java.exe /F

# Wait 5 seconds
Start-Sleep -Seconds 5

# Try again from the beginning
```

### Check Java:
```powershell
java -version
```
If not found, install Java from: https://adoptium.net/

---

## 📍 Files You Need

- ✅ `START-KAFKA.ps1` - Automated startup script (already created)
- ✅ Kafka installed at: `C:\Users\Zeelan\kafka_direct\kafka_2.13-3.6.0`
- ✅ Your app at: `C:\Users\Zeelan\Librarian`

---

## That's It!

Just run the commands above and your Kafka connection error will be fixed! 🎉

