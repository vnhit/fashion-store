@echo off
echo Building FashionStore...
call mvn clean compile
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b %errorlevel%
)

echo Starting FashionStore...
call mvn javafx:run

pause




























