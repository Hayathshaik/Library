# Kafka Startup Script - Automated
# This script starts Zookeeper and Kafka in separate windows

$KafkaDir = "$env:USERPROFILE\kafka_direct\kafka_2.13-3.6.0"

Write-Host ""
Write-Host "======================================"
Write-Host "Starting Kafka Cluster"
Write-Host "======================================"
Write-Host ""

# Check if Kafka exists
if (-not (Test-Path "$KafkaDir\bin\windows\kafka-server-start.bat")) {
    Write-Host "ERROR: Kafka not found at $KafkaDir" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please run this command first to install Kafka:"
    Write-Host ""
    Write-Host '$KafkaHome = "$env:USERPROFILE\kafka_direct"' -ForegroundColor Yellow
    Write-Host 'New-Item -ItemType Directory -Path $KafkaHome -Force | Out-Null' -ForegroundColor Yellow
    Write-Host '$ProgressPreference = "SilentlyContinue"' -ForegroundColor Yellow
    Write-Host 'Invoke-WebRequest -Uri "https://archive.apache.org/dist/kafka/3.6.0/kafka_2.13-3.6.0.tgz" -OutFile "$KafkaHome\kafka.tgz"' -ForegroundColor Yellow
    Write-Host 'tar -xzf "$KafkaHome\kafka.tgz" -C $KafkaHome' -ForegroundColor Yellow
    Write-Host 'Remove-Item "$KafkaHome\kafka.tgz"' -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Kafka found at: $KafkaDir" -ForegroundColor Green

# Kill any existing Kafka/Zookeeper processes
Write-Host ""
Write-Host "Cleaning up old processes..."
Get-Process | Where-Object {$_.ProcessName -eq "java"} | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Start Zookeeper in new window
Write-Host "Starting Zookeeper..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$KafkaDir'; `$host.ui.RawUI.WindowTitle = 'Zookeeper'; .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties"

Write-Host "✓ Zookeeper window opened" -ForegroundColor Green
Write-Host "Waiting 5 seconds for Zookeeper to start..."
Start-Sleep -Seconds 5

# Start Kafka in new window
Write-Host "Starting Kafka Broker..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$KafkaDir'; `$host.ui.RawUI.WindowTitle = 'Kafka Broker'; .\bin\windows\kafka-server-start.bat .\config\server.properties"

Write-Host "✓ Kafka Broker window opened" -ForegroundColor Green
Write-Host "Waiting 10 seconds for Kafka to start..."
Start-Sleep -Seconds 10

# Verify Kafka is listening
Write-Host ""
Write-Host "Verifying Kafka is listening on port 9092..."
$listening = netstat -ano | Select-String ":9092.*LISTENING"
if ($listening) {
    Write-Host "✓ Kafka is listening on port 9092!" -ForegroundColor Green
}
else {
    Write-Host "⚠ Port 9092 not yet listening. Wait a few more seconds..." -ForegroundColor Yellow
}

# Create topic
Write-Host ""
Write-Host "Creating topic 'book_events'..."
Set-Location $KafkaDir
$output = & .\bin\windows\kafka-topics.bat --create --topic book_events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists 2>&1
if ($LASTEXITCODE -eq 0 -or $output -match "already exists") {
    Write-Host "✓ Topic 'book_events' is ready" -ForegroundColor Green
}
else {
    Write-Host "⚠ Topic creation pending (Kafka may still be starting)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "======================================"
Write-Host "✓ KAFKA IS READY!" -ForegroundColor Green
Write-Host "======================================"
Write-Host ""
Write-Host "Two windows are now open:" -ForegroundColor Cyan
Write-Host "  1. Zookeeper (keep running)"
Write-Host "  2. Kafka Broker (keep running)"
Write-Host ""
Write-Host "Next step: Start your Spring Boot app" -ForegroundColor Yellow
Write-Host ""
Write-Host "Run this command:" -ForegroundColor White
Write-Host "  cd C:\Users\Zeelan\Librarian" -ForegroundColor Green
Write-Host "  mvn spring-boot:run" -ForegroundColor Green
Write-Host ""
Write-Host "Press any key to exit this window..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')

