@echo off
echo Starting VictoryGrid Application...

REM Set JavaFX module path (adjust this path to your JavaFX installation)
set JAVAFX_PATH=C:\Users\saids\.m2\repository\org\openjfx

REM Run with JavaFX modules
java --module-path "%JAVAFX_PATH%\javafx-controls\17.0.2; %JAVAFX_PATH%\javafx-fxml\17.0.2; %JAVAFX_PATH%\javafx-graphics\17.0.2; %JAVAFX_PATH%\javafx-base\17.0.2" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp target\classes ^
     com.victorygrid.VictoryGridApp

pause
