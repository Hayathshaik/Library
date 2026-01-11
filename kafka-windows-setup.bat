@echo off
REM Kafka Installation and Startup Script for Windows
REM This script downloads, installs, and starts Kafka on Windows directly (not WSL)

setlocal enabledelayedexpansion

set KAFKA_HOME=%USERPROFILE%\kafka_windows
set KAFKA_DIR=%KAFKA_HOME%\kafka_2.13-3.6.0
set KAFKA_URL=https://archive.apache.org/dist/kafka/3.6.0/kafka_2.13-3.6.0.tgz

echo.
echo ======================================
echo Kafka Setup for Windows (Direct)
echo ======================================
echo.

REM Check if Kafka already installed
if exist "%KAFKA_DIR%\bin\windows\kafka-server-start.bat" (
    echo [OK] Kafka found at %KAFKA_DIR%
) else (
    echo [STEP 1] Creating directories...
    if not exist "%KAFKA_HOME%" mkdir "%KAFKA_HOME%"
    cd /d "%KAFKA_HOME%"

    echo [STEP 2] Downloading Kafka...
    echo URL: %KAFKA_URL%

    REM Use PowerShell to download (more reliable than wget)
    powershell -Command "^
        $ProgressPreference = 'SilentlyContinue'; ^
        try { ^
            $url = '%KAFKA_URL%'; ^
            $out = '%KAFKA_HOME%\kafka.tgz'; ^
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
            Write-Host 'Downloading...'; ^
            Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing; ^
            Write-Host 'Download complete!'; ^
        } catch { ^
            Write-Host 'ERROR: Download failed'; ^
            Write-Host $_.Exception.Message; ^
            exit 1 ^
        } ^
    "

    if !errorlevel! neq 0 (
        echo [ERROR] Download failed. Check your internet connection.
        pause
        exit /b 1
    )

    echo [STEP 3] Extracting Kafka...
    tar -xzf "%KAFKA_HOME%\kafka.tgz" -C "%KAFKA_HOME%"
    if !errorlevel! neq 0 (
        echo [ERROR] Extraction failed
        pause
        exit /b 1
    )
    del "%KAFKA_HOME%\kafka.tgz"
    echo [OK] Kafka installed successfully
)

REM Start Zookeeper
cd /d "%KAFKA_DIR%"
echo.
echo [STEP 4] Starting Zookeeper...
start "Zookeeper" cmd /k "bin\windows\zookeeper-server-start.bat config\zookeeper.properties"
if !errorlevel! neq 0 (
    echo [ERROR] Failed to start Zookeeper
    pause
    exit /b 1
)

echo [OK] Zookeeper started in new window
timeout /t 3 /nobreak

REM Start Kafka
echo [STEP 5] Starting Kafka Broker...
start "Kafka Broker" cmd /k "bin\windows\kafka-server-start.bat config\server.properties"
if !errorlevel! neq 0 (
    echo [ERROR] Failed to start Kafka
    pause
    exit /b 1
)

echo [OK] Kafka Broker started in new window
timeout /t 5 /nobreak

REM Create topic
echo [STEP 6] Creating topic 'book_events'...
cd /d "%KAFKA_DIR%"
call bin\windows\kafka-topics.bat --create --topic book_events ^
    --bootstrap-server localhost:9092 ^
    --partitions 1 ^
    --replication-factor 1 ^
    --if-not-exists 2>nul

echo.
echo ======================================
echo [SUCCESS] Kafka is Ready!
echo ======================================
echo.
echo Bootstrap Servers: localhost:9092
echo Topic: book_events
echo.
echo Two windows should be open:
echo  1. Zookeeper (keep running)
echo  2. Kafka Broker (keep running)
echo.
echo Now run your Spring Boot app:
echo   mvn spring-boot:run
echo.
pause

