# Kafka Startup Script for Windows
# Downloads and starts Kafka with Zookeeper

$KafkaVersion = "3.6.0"
$ScalaVersion = "2.13"
$KafkaHome = "$env:USERPROFILE\kafka"
$KafkaDir = "$KafkaHome\kafka_$($ScalaVersion)-$($KafkaVersion)"

Write-Host "======================================" -ForegroundColor Green
Write-Host "Kafka Setup for Windows" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# Check if Kafka is already installed
if (Test-Path $KafkaDir) {
    Write-Host "✓ Kafka found at $KafkaDir" -ForegroundColor Green
} else {
    Write-Host "→ Installing Kafka $KafkaVersion..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $KafkaHome | Out-Null

    $KafkaUrl = "https://archive.apache.org/dist/kafka/$KafkaVersion/kafka_$($ScalaVersion)-$($KafkaVersion).tgz"
    $KafkaFile = "$KafkaHome\kafka.tgz"

    Write-Host "  Downloading from: $KafkaUrl"
    Invoke-WebRequest -Uri $KafkaUrl -OutFile $KafkaFile -UseBasicParsing

    # Extract using 7-Zip or tar if available
    Write-Host "  Extracting..."
    if (Get-Command tar -ErrorAction SilentlyContinue) {
        tar -xzf $KafkaFile -C $KafkaHome
    } else {
        Write-Host "ERROR: tar command not found. Please install Git for Windows or 7-Zip" -ForegroundColor Red
        exit 1
    }

    Remove-Item $KafkaFile
    Write-Host "✓ Kafka installed successfully" -ForegroundColor Green
}

# Start Zookeeper
Write-Host ""
Write-Host "→ Starting Zookeeper..." -ForegroundColor Yellow
$ZookeeperProcess = Start-Process -FilePath "$KafkaDir\bin\windows\zookeeper-server-start.bat" `
    -ArgumentList "$KafkaDir\config\zookeeper.properties" `
    -NoNewWindow -PassThru
Write-Host "✓ Zookeeper started (PID: $($ZookeeperProcess.Id))" -ForegroundColor Green

# Wait for Zookeeper to be ready
Start-Sleep -Seconds 3

# Start Kafka broker
Write-Host "→ Starting Kafka broker..." -ForegroundColor Yellow
$KafkaProcess = Start-Process -FilePath "$KafkaDir\bin\windows\kafka-server-start.bat" `
    -ArgumentList "$KafkaDir\config\server.properties" `
    -NoNewWindow -PassThru
Write-Host "✓ Kafka started (PID: $($KafkaProcess.Id))" -ForegroundColor Green

# Wait for Kafka to be ready
Start-Sleep -Seconds 5

# Verify Kafka is listening
Write-Host ""
Write-Host "→ Verifying Kafka is listening on port 9092..." -ForegroundColor Yellow
$Listening = netstat -ano | Select-String ":9092"
if ($Listening) {
    Write-Host "✓ Kafka is listening on port 9092" -ForegroundColor Green
} else {
    Write-Host "✗ Kafka is NOT listening on port 9092" -ForegroundColor Red
    exit 1
}

# Create the book_events topic
Write-Host ""
Write-Host "→ Creating topic 'book_events'..." -ForegroundColor Yellow
& "$KafkaDir\bin\windows\kafka-topics.bat" --create --topic book_events `
    --bootstrap-server localhost:9092 `
    --partitions 1 `
    --replication-factor 1 `
    --if-not-exists 2>$null

Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "✓ Kafka is ready!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""
Write-Host "Bootstrap Servers: localhost:9092" -ForegroundColor Cyan
Write-Host "Topic: book_events" -ForegroundColor Cyan
Write-Host ""
Write-Host "Your Spring Boot app should now connect successfully!" -ForegroundColor Green

