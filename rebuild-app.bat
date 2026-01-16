@echo off
echo ============================================
echo Rebuilding Librarian Application
echo ============================================
echo.

REM Navigate to project directory
cd /d C:\Users\Zeelan\Librarian

REM Build with Maven
echo Building project with Maven...
call mvn clean compile -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo BUILD SUCCESS!
    echo.
    echo Starting application...
    timeout /t 3 /nobreak
    call mvn spring-boot:run
) else (
    echo.
    echo BUILD FAILED!
    echo Please check the logs above.
    timeout /t 10 /nobreak
)

