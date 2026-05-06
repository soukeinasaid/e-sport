@echo off
echo =================================
echo   VictoryGrid - Database Setup & Run
echo =================================

echo.
echo Step 1: Setting up database...
"C:\Program Files (x86)\Java\jdk1.8.0_202\bin\java.exe" -cp "target\classes;lib\mysql-connector-j-8.0.33.jar" utilies.DatabaseSetup

if %ERRORLEVEL% neq 0 (
    echo ERROR: Database setup failed
    echo Continuing with compilation...
)

echo.
echo Step 2: Compiling and running application...
call compile_and_run.bat

pause
