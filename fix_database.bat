@echo off
echo =================================
echo   Database Setup for VictoryGrid
echo =================================

echo.
echo Setting up database tables...
echo Make sure MySQL is running and accessible

echo.
echo Running database setup script...
mysql -u root -p < database_setup.sql

if %ERRORLEVEL% neq 0 (
    echo ERROR: Database setup failed
    echo Make sure MySQL is installed and running
    echo Check your MySQL credentials
    pause
    exit /b 1
)

echo.
echo Database setup completed successfully!
echo All tables have been created and sample data inserted.

pause
