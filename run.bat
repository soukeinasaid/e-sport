@echo off
echo =================================
echo   VictoryGrid E-Sports Forum
echo =================================

echo.
echo Checking Java installation...
java -version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo.
echo Compiling the project...
if not exist target\classes mkdir target\classes

javac -cp ".;lib\*" -d target\classes ^
    src\main\java\utilies\*.java ^
    src\main\java\entity\*.java ^
    src\main\java\service\*.java ^
    src\main\java\controller\*.java ^
    RunApp.java

if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation failed
    echo Make sure you have JavaFX and MySQL connector in the lib folder
    pause
    exit /b 1
)

echo.
echo Starting the application...
java -cp ".;target\classes;lib\*" RunApp

pause
