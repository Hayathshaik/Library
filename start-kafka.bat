@echo off
REM Simple Kafka Startup - Opens Windows for Zookeeper and Kafka

set KAFKA_DIR=%USERPROFILE%\kafka_direct\kafka_2.13-3.6.0

if not exist "%KAFKA_DIR%\bin\windows\kafka-server-start.bat" (
    echo.
    echo ERROR: Kafka not found at %KAFKA_DIR%
    echo.
    echo Please run this PowerShell command first:
    echo.
    echo $KafkaHome = "$env:USERPROFILE\kafka_direct"
    echo $KafkaDir = "$KafkaHome\kafka_2.13-3.6.0"
    echo if (-not (Test-Path "$KafkaDir")) {
    echo   New-Item -ItemType Directory -Path $KafkaHome -Force ^| Out-Null
    echo   $ProgressPreference = 'SilentlyContinue'
    echo   Invoke-WebRequest -Uri "https://archive.apache.org/dist/kafka/3.6.0/kafka_2.13-3.6.0.tgz" -OutFile "$KafkaHome\kafka.tgz" -UseBasicParsing
    echo   tar -xzf "$KafkaHome\kafka.tgz" -C $KafkaHome
    echo   Remove-Item "$KafkaHome\kafka.tgz"
    echo   Write-Host "Kafka ready!"
    echo }
    echo.
    pause
    exit /b 1
)

cd /d "%KAFKA_DIR%"

echo.
echo ======================================
echo Starting Kafka Cluster
echo ======================================
echo.

echo Starting Zookeeper in new window...
start "Zookeeper" cmd /k "bin\windows\zookeeper-server-start.bat config\zookeeper.properties"

echo Waiting 3 seconds for Zookeeper to start...
timeout /t 3 /nobreak

echo Starting Kafka Broker in new window...
start "Kafka Broker" cmd /k "bin\windows\kafka-server-start.bat config\server.properties"

echo Waiting 5 seconds for Kafka to start...
timeout /t 5 /nobreak

echo.
echo ======================================
echo Creating Topic
echo ======================================
call bin\windows\kafka-topics.bat --create --topic book_events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists

echo.
echo ======================================
echo SUCCESS!
echo ======================================
echo.
echo Kafka Broker: localhost:9092
echo Topic: book_events
echo.
echo You should see 2 new windows:
echo  - Zookeeper (keep it running)
echo  - Kafka Broker (keep it running)
echo.
echo Now run your Spring Boot app:
echo  cd C:\Users\Zeelan\Librarian
echo  mvn spring-boot:run
echo.
pause

