@echo off
REM Kafka Stop Script for Windows

echo Stopping Kafka and Zookeeper...
echo.

echo Stopping Kafka broker...
taskkill /IM java.exe /F 2>nul

echo Waiting 2 seconds...
timeout /t 2 /nobreak

echo.
echo ========================================
echo Kafka and Zookeeper stopped!
echo ========================================
echo.
pause

