@echo off
REM Kafka Quick Startup Script for Windows

setlocal enabledelayedexpansion

set KAFKA_HOME=%USERPROFILE%\kafka
set KAFKA_DIR=%KAFKA_HOME%\kafka_2.13-3.6.0

if not exist "%KAFKA_DIR%" (
    echo Downloading Kafka...
    cd /d "%KAFKA_HOME%" 2>nul || (
        mkdir "%KAFKA_HOME%"
        cd /d "%KAFKA_HOME%"
    )

    powershell -Command "(New-Object System.Net.ServicePointManager).SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/kafka/3.6.0/kafka_2.13-3.6.0.tgz' -OutFile 'kafka.tgz' -UseBasicParsing; tar -xzf kafka.tgz; del kafka.tgz"
    echo Kafka installed!
)

cd /d "%KAFKA_DIR%"

echo Starting Zookeeper...
start "Zookeeper" cmd /k bin\windows\zookeeper-server-start.bat config\zookeeper.properties

timeout /t 3 /nobreak

echo Starting Kafka Broker...
start "Kafka Broker" cmd /k bin\windows\kafka-server-start.bat config\server.properties

timeout /t 5 /nobreak

echo Creating topic 'book_events'...
bin\windows\kafka-topics.bat --create --topic book_events ^
    --bootstrap-server localhost:9092 ^
    --partitions 1 ^
    --replication-factor 1 ^
    --if-not-exists

echo.
echo ========================================
echo Kafka is ready!
echo Bootstrap: localhost:9092
echo Topic: book_events
echo ========================================
pause

